from pathlib import Path
import tempfile
import unittest

from scripts.check_locale_resources import audit_repository, compare_catalog, placeholder_signature, read_catalog


class LocaleResourceChecksTest(unittest.TestCase):
    def test_positional_reordering_preserves_the_same_arguments(self):
        self.assertEqual(placeholder_signature("%1$s: %2$d"), placeholder_signature("%2$d — %1$s"))
        self.assertEqual(placeholder_signature("%s / %d"), placeholder_signature("%1$s / %2$d"))

    def test_type_change_and_dropped_repeated_argument_are_detected(self):
        result = compare_catalog({"type": "%1$d", "repeat": "%1$s %1$s"}, {"type": "%1$s", "repeat": "%1$s"})
        self.assertEqual(["repeat", "type"], result["placeholder_mismatches"])

    def test_literal_percent_and_newline_do_not_consume_arguments(self):
        self.assertEqual(placeholder_signature("%% %n %s"), placeholder_signature("%1$s"))

    def test_relative_and_date_placeholders_keep_argument_identity(self):
        self.assertEqual(placeholder_signature("%1$tY %<tm"), placeholder_signature("%1$tY %1$tm"))
        with self.assertRaises(ValueError):
            placeholder_signature("%<s")

    def test_missing_extra_and_empty_strings_are_detected(self):
        result = compare_catalog({"a": "A", "b": "B"}, {"a": " ", "c": "C"})
        self.assertEqual(["b"], result["missing"])
        self.assertEqual(["c"], result["extra"])
        self.assertEqual(["a"], result["empty"])

    def test_duplicate_keys_across_resource_files_are_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            for file in ("strings.xml", "other.xml"):
                (directory / file).write_text('<resources><string name="a">A</string></resources>')
            with self.assertRaisesRegex(ValueError, "Duplicate string a"):
                read_catalog(directory)

    def test_all_supported_resources_and_runtime_string_references_are_complete(self):
        report = audit_repository(Path(__file__).resolve().parents[2])
        self.assertEqual([], report["unresolved_string_references"])
        for locale, result in report["locales"].items():
            with self.subTest(locale=locale):
                for key in ("missing", "extra", "placeholder_mismatches", "empty"):
                    self.assertEqual([], result[key], key)


if __name__ == "__main__":
    unittest.main()
