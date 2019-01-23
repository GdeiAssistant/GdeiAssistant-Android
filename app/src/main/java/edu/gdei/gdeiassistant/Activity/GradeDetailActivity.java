package edu.gdei.gdeiassistant.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import edu.gdei.gdeiassistant.R;

public class GradeDetailActivity extends AppCompatActivity {

    private Toolbar gradeDetailToolbar;

    private TextView gradeDetailSubjectName;
    private TextView gradeDetailSubjectScore;
    private TextView gradeDetailSubjectCredit;
    private TextView gradeDetailSubjectGPA;
    private TextView gradeDetailSubjectId;
    private TextView gradeDetailSubjectYear;
    private TextView gradeDetailSubjectType;
    private TextView gradeDetailSubjectTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_detail);
        InitView();
    }

    private void InitView() {
        gradeDetailToolbar = (Toolbar) findViewById(R.id.gradeDetailToolbar);
        setSupportActionBar(gradeDetailToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        gradeDetailSubjectName = (TextView) findViewById(R.id.gradeDetailSubjectName);
        gradeDetailSubjectScore = (TextView) findViewById(R.id.gradeDetailSubjectScore);
        gradeDetailSubjectCredit = (TextView) findViewById(R.id.gradeDetailSubjectCredit);
        gradeDetailSubjectGPA = (TextView) findViewById(R.id.gradeDetailSubjectGPA);
        gradeDetailSubjectId = (TextView) findViewById(R.id.gradeDetailSubjectId);
        gradeDetailSubjectYear = (TextView) findViewById(R.id.gradeDetailSubjectYear);
        gradeDetailSubjectType = (TextView) findViewById(R.id.gradeDetailSubjectType);
        gradeDetailSubjectTerm = (TextView) findViewById(R.id.gradeDetailSubjectTerm);
        gradeDetailSubjectName.setText(getIntent().getStringExtra("SubjectName"));
        gradeDetailSubjectScore.setText(getIntent().getStringExtra("SubjectScore"));
        gradeDetailSubjectCredit.setText(getIntent().getStringExtra("SubjectCredit"));
        gradeDetailSubjectGPA.setText(getIntent().getStringExtra("SubjectGPA"));
        gradeDetailSubjectId.setText(getIntent().getStringExtra("SubjectId"));
        gradeDetailSubjectYear.setText(getIntent().getStringExtra("SubjectYear"));
        gradeDetailSubjectType.setText(getIntent().getStringExtra("SubjectType"));
        gradeDetailSubjectTerm.setText(getIntent().getStringExtra("SubjectTerm"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //返回上一层
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
