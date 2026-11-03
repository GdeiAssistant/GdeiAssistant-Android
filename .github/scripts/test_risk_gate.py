#!/usr/bin/env python3
"""Execute the real github-script bodies with offline GitHub API doubles."""
import json
import os
import re
import subprocess
import unittest
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = yaml.load((ROOT / '.github/workflows/risk-gate.yml').read_text(), Loader=yaml.BaseLoader)
SHA = 'a' * 40

# Only explicitly mocked endpoints exist; no Octokit client or network is used.
HARNESS = r'''
const fs = require('fs');
const input = JSON.parse(fs.readFileSync(0, 'utf8'));
const f = input.fixture;
const calls = [], statuses = [], comments = [], failures = [];
let prReads = 0, reviewReads = 0;
const github = {rest: {
  pulls: {get: async args => {
    calls.push(['pulls.get', args]);
    if (f.getError) throw new Error('mock PR API failure');
    if (input.job === 'targets') return {data: f.prs[String(args.pull_number)]};
    return {data: ++prReads > 1 && f.current ? f.current : f.pr};
  }, listFiles: 'files', listReviews: 'reviews', list: 'open', listCommits: 'commits'},
  issues: {listEvents: 'events', listComments: 'comments', createComment: async args => comments.push(args)},
  repos: {listPullRequestsAssociatedWithCommit: 'associated', get: async () => ({data: {stargazers_count: 0}}),
    createCommitStatus: async args => statuses.push(args)}
}, paginate: async (endpoint, args) => {
  calls.push([endpoint, args]);
  if (f.errors?.includes(endpoint)) throw new Error('mock ' + endpoint + ' API failure');
  if (endpoint === 'reviews') return f.reviewReads ? f.reviewReads[reviewReads++] : (f.reviews || []);
  if (endpoint === 'comments') return [];
  if (!Object.hasOwn(f, endpoint)) throw new Error('Unmocked API: ' + endpoint);
  return f[endpoint];
}};
const core = {setFailed: message => failures.push(message), warning: () => {}};
const context = {repo: input.repository, serverUrl: 'https://example.invalid',
  runId: 1, eventName: input.event || 'pull_request_target', payload: f.payload || {pull_request: f.pr}};
(async () => {
  let result, error;
  try {
    result = await new (Object.getPrototypeOf(async function(){}).constructor)('github', 'context', 'core', 'require', input.script)(github, context, core, require);
  } catch (e) { error = e.message; }
  process.stdout.write(JSON.stringify({result, error, statuses, comments, failures, calls}));
})();
'''


def label_event(event_id=1, event='labeled', actor='fake-owner'):
    return {'id': event_id, 'event': event, 'label': {'name': 'owner-approved'}, 'actor': {'login': actor}}


def review(state='APPROVED', sha=SHA, review_id=1):
    return {'id': review_id, 'state': state, 'commit_id': sha,
            'submitted_at': '2026-01-01T00:00:00Z', 'user': {'login': 'coderabbitai[bot]', 'type': 'Bot'}}


class ScriptTests(unittest.TestCase):
    def run_script(self, fixture, job='evaluate', event=None):
        script = next(step['with']['script'] for step in WORKFLOW['jobs'][job]['steps'] if 'script' in step.get('with', {}))
        # Exercise owner provenance with a synthetic identity, never a real account.
        script, count = re.subn(r"(latest\.actor\?\.login === )'[^']+'", r"\1'fake-owner'", script)
        self.assertEqual(count, 1, 'Owner provenance must be checked explicitly')
        result = subprocess.run(['node', '-e', HARNESS], input=json.dumps({
            'fixture': fixture, 'script': script, 'job': job, 'event': event,
            'repository': dict(zip(('owner', 'repo'), json.loads((ROOT / '.github/scripts/dependabot-policy.json').read_text())['repo'].split('/'))),
        }), capture_output=True, text=True, check=True, timeout=10,
            cwd=ROOT, env={'PATH': os.environ['PATH'], 'PR_NUMBER': '7',
                'DEPENDABOT_DEPENDENCIES': fixture.get('dependency_json', 'null')})
        return json.loads(result.stdout)

    def fixture(self, labels=True, events=None, files=None, reviews=None, **extra):
        files = files if files is not None else [{'filename': '.github/workflows/build.yml'}]
        return {'pr': {'user': {'login': 'synthetic-contributor'}, 'number': 7, 'state': 'open', 'head': {'sha': SHA}, 'changed_files': len(files),
                       'labels': [{'name': 'owner-approved'}] if labels else [],
                       'body': '## AI assistance\nCoding assistant.\n## What changed\nGate validation.\n## How verified\nOffline regression tests.\n'},
                'files': files, 'events': events if events is not None else [label_event()],
                'reviews': reviews or [], **extra}

    def assert_gate(self, fixture, allowed):
        result = self.run_script(fixture)
        states = [s['state'] for s in result['statuses'] if s['context'] == 'risk-gate']
        self.assertTrue(states, result)
        if allowed:
            self.assertEqual(states[-1], 'success', result)
            self.assertFalse(result.get('error'), result)
            self.assertFalse(result['failures'], result)
        else:
            self.assertIn(states[-1], ('failure', 'error'), result)
            self.assertTrue(result.get('error') or result['failures'], result)
        return result

    def test_right_actor_passes(self):
        self.assert_gate(self.fixture(), True)

    def test_wrong_actor_fails(self):
        self.assert_gate(self.fixture(events=[label_event(actor='other')]), False)

    def test_latest_unlabeled_fails(self):
        self.assert_gate(self.fixture(events=[label_event(9, 'unlabeled'), label_event(3)]), False)

    def test_latest_wrong_actor_overrides_old_owner(self):
        self.assert_gate(self.fixture(events=[label_event(9, actor='other'), label_event(3)]), False)

    def test_no_event_fails(self):
        self.assert_gate(self.fixture(events=[]), False)

    def test_event_api_error_fails_closed_even_with_bot_approval(self):
        self.assert_gate(self.fixture(errors=['events'], reviews=[review()]), False)

    def test_unknown_event_id_fails_closed(self):
        self.assert_gate(self.fixture(events=[label_event(None)]), False)

    def test_missing_actor_fails(self):
        event = label_event()
        del event['actor']
        self.assert_gate(self.fixture(events=[event]), False)

    def test_label_absent_does_not_use_event(self):
        self.assert_gate(self.fixture(labels=False), False)

    def test_bot_approval_without_owner_label_cannot_authorize(self):
        result = self.assert_gate(self.fixture(labels=False, reviews=[review()]), False)
        self.assertNotIn('events', [name for name, _ in result['calls']])

    def test_wrong_actor_cannot_use_bot_approval(self):
        self.assert_gate(self.fixture(events=[label_event(actor='other')], reviews=[review()]), False)

    def test_stale_bot_approval_fails(self):
        self.assert_gate(self.fixture(labels=False, reviews=[review(sha='b' * 40)]), False)

    def test_latest_nonapproval_blocks_earlier_bot_approval(self):
        for state in ('COMMENTED', 'CHANGES_REQUESTED', 'DISMISSED'):
            with self.subTest(state=state):
                self.assert_gate(self.fixture(labels=False, reviews=[review(), review(state, review_id=2)]), False)

    def test_coderabbit_unavailable_is_not_a_dependency(self):
        for files in ([{'filename': 'docs/guide.md'}], [{'filename': '.github/workflows/build.yml'}]):
            with self.subTest(files=files):
                result = self.assert_gate(self.fixture(files=files, errors=['reviews']), True)
                self.assertNotIn('reviews', [name for name, _ in result['calls']])

    def test_coderabbit_cannot_change_gate_outcome(self):
        for state in ('APPROVED', 'COMMENTED', 'CHANGES_REQUESTED', 'DISMISSED', 'UNKNOWN'):
            for labels in (False, True):
                with self.subTest(state=state, labels=labels):
                    self.assert_gate(self.fixture(labels=labels, reviews=[review(state)]), labels)

    def test_low_risk_passes_when_coderabbit_is_skipped_or_unavailable(self):
        self.assert_gate(self.fixture(labels=False, files=[{'filename': 'docs/guide.md'}], errors=['reviews']), True)

    def test_pr_head_race_fails_closed(self):
        fixture = self.fixture()
        fixture['current'] = dict(fixture['pr'], head={'sha': 'b' * 40})
        self.assert_gate(fixture, False)

    def test_low_risk_still_passes(self):
        self.assert_gate(self.fixture(labels=False, files=[{'filename': 'docs/guide.md'}]), True)

    def test_incomplete_diff_fails_closed(self):
        fixture = self.fixture()
        fixture['pr']['changed_files'] += 1
        self.assert_gate(fixture, False)

    def test_master_trusted_policy_and_concurrency_are_retained(self):
        self.assertEqual(WORKFLOW['permissions'], {
            'contents': 'read', 'pull-requests': 'read', 'issues': 'read', 'statuses': 'write'})
        self.assertEqual(WORKFLOW['concurrency']['cancel-in-progress'], 'true')
        self.assertNotIn('workflow_run', WORKFLOW['on'])
        self.assertEqual(set(WORKFLOW['jobs']), {'evaluate'})
        checkout = WORKFLOW['jobs']['evaluate']['steps'][0]
        self.assertEqual(checkout['with']['persist-credentials'], 'false')
        self.assertEqual(checkout['with']['ref'], '${{ github.event.repository.default_branch }}')

    def test_pr_body_and_label_races_fail_closed(self):
        for change in ({'body': 'replaced'}, {'labels': []}, {'state': 'closed'}):
            with self.subTest(change=change):
                fixture = self.fixture()
                fixture['current'] = dict(fixture['pr'], **change)
                self.assert_gate(fixture, False)

    def test_bad_template_is_still_blocking(self):
        fixture = self.fixture()
        fixture['pr']['body'] = 'Incomplete template'
        result = self.run_script(fixture)
        self.assertEqual(result['statuses'][-1]['state'], 'failure', result)
        self.assertTrue(result['failures'], result)

    def test_verified_dependabot_template_policy_is_retained(self):
        repository = json.loads((ROOT / '.github/scripts/dependabot-policy.json').read_text())['repo']
        fixture = self.fixture(labels=False, files=[{'filename': 'gradle/libs.versions.toml', 'status': 'modified'}])
        fixture['pr'].update(user={'login': 'dependabot[bot]'}, draft=False, commits=1, body='Dependency update',
            base={'ref': 'master', 'repo': {'full_name': repository}})
        fixture['pr']['head']['repo'] = {'full_name': repository}
        fixture['commits'] = [{'sha': SHA, 'author': {'login': 'dependabot[bot]'},
            'committer': {'login': 'web-flow'}, 'commit': {'committer': {'email': 'noreply@' + 'github' + '.com'},
                'verification': {'verified': True}}}]
        result = self.assert_gate(fixture, True)
        self.assertIn('Verified Dependabot', result['statuses'][-1]['description'])

    def test_unverified_dependabot_cannot_bypass_template(self):
        fixture = self.fixture(labels=False, files=[{'filename': 'gradle/libs.versions.toml', 'status': 'modified'}])
        fixture['pr'].update(user={'login': 'dependabot[bot]'}, body='Dependency update')
        fixture['commits'] = []
        result = self.run_script(fixture)
        self.assertEqual(result['statuses'][-1]['state'], 'failure', result)
        self.assertTrue(result['failures'], result)


class OwnerOnlyTests(unittest.TestCase):
    run_script = ScriptTests.run_script
    fixture = ScriptTests.fixture
    assert_gate = ScriptTests.assert_gate

    # Both endpoints of a rename matter, including guidance below docs/tests.
    def control_files(self):
        for path in ('.coderabbit.yaml', '.coderabbit.yml', 'AGENTS.md', '.github/engineering.md',
                     'docs/AGENTS.md', 'tests/AGENTS.md'):
            yield {'filename': path}
            yield {'filename': 'docs/ordinary.md', 'previous_filename': path}
            yield {'filename': path, 'previous_filename': 'docs/ordinary.md'}

    def test_control_plane_requires_owner(self):
        for file in self.control_files():
            with self.subTest(file=file):
                self.assert_gate(self.fixture(labels=False, files=[file], reviews=[review()]), False)
                self.assert_gate(self.fixture(files=[file], reviews=[review()]), True)

    def test_bot_cannot_authorize_mixed_control_and_workflow_diff(self):
        self.assert_gate(self.fixture(labels=False, reviews=[review()], files=[
            {'filename': '.github/workflows/build.yml'}, {'filename': '.coderabbit.yaml'},
        ]), False)

    def test_coderabbit_configuration_is_advisory(self):
        config = yaml.safe_load((ROOT / '.coderabbit.yaml').read_text())
        self.assertFalse(config['reviews']['request_changes_workflow'])
        self.assertFalse(config['reviews']['auto_approvals']['enabled'])


if __name__ == '__main__':
    unittest.main()
