package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.DataBasePostResponse;
import jp.kozu_osaka.android.kozuzen.access.argument.post.TentativeRegisterArguments;
import jp.kozu_osaka.android.kozuzen.access.callback.PostAccessCallBack;
import jp.kozu_osaka.android.kozuzen.access.request.post.TentativeRegisterRequest;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccountManager;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.MailAddressChecker;
import jp.kozu_osaka.android.kozuzen.security.PasswordChecker;
import jp.kozu_osaka.android.kozuzen.security.Secrets;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;
import jp.kozu_osaka.android.kozuzen.security.TermChecker;
import jp.kozu_osaka.android.kozuzen.util.Logger;
import jp.kozu_osaka.android.kozuzen.util.ZenActionModeCallback;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;

public final class CreateAccountActivity extends AppCompatActivity {

    /**
     * 部活やSNSで未所属などを選択したかどうか。未所属などを選択した際にほかの選択肢を選べないようにするため。
     */
    private boolean selectedClubNone;
    /**
     * 帰宅部以外の選択したクラブ。
     */
    private int selectedClubOther;
    private boolean selectedSNSNone;
    private int selectedSNSOther;

    /**
     * アカウント作成時に部活やSNSを尋ねるとき、部活名のチェックボックスを
     * 平行方向一列当たりいくつ配置するか。
     */
    private static final int ITEMS_PER_HORIZONAL = 2;

    /**
     * 所属クラブの選択画面において、[所属無し]を選んだ場合の所作。
     */
    private final CompoundButton.OnCheckedChangeListener noneClubListener = (b, checked) -> { //帰宅部選択時、selectedClubNoneをtrueに
        CreateAccountActivity.this.selectedClubNone = checked;
        if(CreateAccountActivity.this.selectedClubOther >= 1) {
            b.setChecked(false);
            this.selectedClubNone = false;
        }
    };
    /**
     * 所属クラブの選択画面において、[所属無し]以外を選んだ場合の所作。
     */
    private final CompoundButton.OnCheckedChangeListener otherClubListener = (b, checked) -> { //帰宅部が選択されている場合、そのほかのチェックを阻止する
        CreateAccountActivity.this.selectedClubOther += checked ? 1 : -1;
        if(CreateAccountActivity.this.selectedClubNone) {
            b.setChecked(false);
            this.selectedClubOther--;
        }
    };

    /**
     * 使っているSNSを答える質問の中で、[使っているSNSが特になし]を選択したときの動作
     */
    private final CompoundButton.OnCheckedChangeListener noneSNSListener = (b, checked) -> {
        CreateAccountActivity.this.selectedSNSNone = checked;
        if(CreateAccountActivity.this.selectedSNSOther >= 1) {
            b.setChecked(false);
            this.selectedSNSNone = false;
        }
    };
    /**
     * 使っているSNSを答える質問の中で、[使っているSNSが特になし]を以外選択したときの動作
     */
    private final CompoundButton.OnCheckedChangeListener otherSNSListener = (b, checked) -> { //NOneが選択されている場合、そのほかのチェックを阻止する
        CreateAccountActivity.this.selectedSNSOther += checked ? 1 : -1;
        if(CreateAccountActivity.this.selectedSNSNone) {
            b.setChecked(false);
            this.selectedSNSOther--;
        }
    };

    /**
     * パスワード入力時、文字列選択時のメニューからコピー、ペースト、カットを削除する。<br>
     * これによってセキュリティの向上を目指す。
     */
    private final ActionMode.Callback actionModeCallback = new ZenActionModeCallback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuItem itemCopy = menu.findItem(android.R.id.copy);
            MenuItem itemCut = menu.findItem(android.R.id.cut);
            MenuItem itemPaste = menu.findItem(android.R.id.paste);
            MenuItem itemPastePlain = menu.findItem(android.R.id.pasteAsPlainText);

            if (itemCopy != null) menu.removeItem(android.R.id.copy);
            if (itemCut != null) menu.removeItem(android.R.id.cut);
            if(itemPaste != null) menu.removeItem(android.R.id.paste);
            if(itemPastePlain != null) menu.removeItem(android.R.id.pasteAsPlainText);
            return true;
        }
    };

    /**
     * 期生の入力時のチェック。
     */
    private final TextWatcher whenTermInput = new ZenTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText view = findViewById(R.id.editText_createAccount_term);
            try {
                view.setError(TermChecker.checkTerm(view.getText().toString())
                        ? null : getString(R.string.text_createAccount_warn_term));
            } catch(NumberFormatException e) {
                view.setError(getString(R.string.text_createAccount_warn_term));
            }
        }
    };

    /**
     * メールアドレスの入力時のチェック。
     */
    private final TextWatcher whenMailInput = new ZenTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText view = findViewById(R.id.editText_createAccount_mail);
            view.setError(MailAddressChecker.checkMailAddress(view.getText().toString())
                    ? null : getString(R.string.text_createAccount_warn_mailAddress));
        }
    };

    /**
     * パスワードの入力時のチェック。
     */
    private final TextWatcher whenPassInput = new ZenTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText view = findViewById(R.id.editText_createAccount_pass);
            PasswordChecker.SafetyStatus status = PasswordChecker.checkPassword(view.getText().toString());
            String msg = "";
            if(!status.isOnlyAlnumsAndSymbols()) {
                msg += getString(R.string.text_createAccount_passwordCondition_warn1) + "\n";
            }
            if(!status.isRangeInLimit()) {
                msg += getString(R.string.text_createAccount_passwordCondition_warn2) + "\n";
            }
            if(!status.meetsMinLenOfAlnumsAndSymbols()) {
                msg += getString(R.string.text_createAccount_passwordCondition_warn3) + "\n";
            }
            view.setError(msg.isEmpty() ? null : msg);
        }
    };

    private final TextWatcher whenPassCheckInput = new ZenTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText pass = findViewById(R.id.editText_createAccount_pass);
            EditText passCheck = findViewById(R.id.editText_createAccount_passCheck);
            passCheck.setError(passCheck.getText().toString().equals(pass.getText().toString()) ?
                    null : getString(R.string.text_createAccount_warn_passwordMatch));
        }
    };

    private final TextWatcher whenGradeCheckInput = new ZenTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText grade = findViewById(R.id.editText_createAccount_grade);
            String enteredGradeStr = grade.getText().toString();
            if(enteredGradeStr.isEmpty()) {
                grade.setError(getString(R.string.text_createAccount_warn_grade_empty));
            } else {
                int enteredGrade = Integer.parseInt(enteredGradeStr);
                if(!(1 <= enteredGrade && enteredGrade <= 2)) {
                    grade.setError(getString(R.string.text_createAccount_warn_grade_invalid));
                } else {
                    grade.setError(null);
                }
            }
        }
    };

    private final TextWatcher whenClassCheckInput = new ZenTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText clazz = findViewById(R.id.editText_createAccount_class);
            String enteredClassStr = clazz.getText().toString();
            if(enteredClassStr.isEmpty()) {
                clazz.setError(getString(R.string.text_createAccount_warn_class_empty));
            } else {
                int enteredGrade = Integer.parseInt(enteredClassStr);
                if(!(1 <= enteredGrade && enteredGrade <= 9)) {
                    clazz.setError(getString(R.string.text_createAccount_warn_class_invalid));
                } else {
                    clazz.setError(null);
                }
            }
        }
    };

    private final TextWatcher whenNumberCheckInput = new ZenTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText number = findViewById(R.id.editText_createAccount_number);
            String enteredNumberStr = number.getText().toString();
            if(enteredNumberStr.isEmpty()) {
                number.setError(getString(R.string.text_createAccount_warn_number_empty));
            } else {
                int enteredGrade = Integer.parseInt(enteredNumberStr);
                if(!(1 <= enteredGrade && enteredGrade <= 9)) {
                    number.setError(getString(R.string.text_createAccount_warn_number_invalid));
                } else {
                    number.setError(null);
                }
            }
        }
    };

    /**
     * 「登録」ボタンを押したときの登録処理。
     */
    private final View.OnClickListener whenEnterClicked = v -> {
        boolean isValidAnswer = true;

        int term = checkTerm();
        List<SignupQuestion.Club> clubs = checkCheckedClub();
        List<SignupQuestion.SNS> sns = checkCheckedSNS();
        SignupQuestion.AgeLevel age = checkCheckedAge();
        SignupQuestion.MotivationLevel motivation = checkMotivationLevel();
        int motivationHour = checkMotivationHour();
        int motivationMinute = checkMotivationMinute();
        SignupQuestion.Rule rule = checkRule();
        SignupQuestion.DependenceLevel dependence = checkDependenceLevel();

        boolean isReadPolicy = checkReadPolicy();
        boolean isValidMail = checkMailAddress();
        boolean isValidPass = checkPassword();
        boolean isValidPassCheck = checkPasswordCheck();
        boolean isValidGrade = checkGrade();
        boolean isValidClass = checkClass();
        boolean isValidNumber = checkNumber();

        //判定
        if(clubs.isEmpty()) isValidAnswer = false;
        if(sns.isEmpty()) isValidAnswer = false;
        if(age.equals(SignupQuestion.AgeLevel.NON_SELECTED)) isValidAnswer = false;
        if(motivation == null) isValidAnswer = false;
        if(rule == null) isValidAnswer = false;
        if(dependence == null) isValidAnswer = false;
        if(!isReadPolicy) isValidAnswer = false;
        if(!isValidMail) isValidAnswer = false;
        if(!isValidPass) isValidAnswer = false;
        if(!isValidPassCheck) isValidAnswer = false;
        if(!isValidGrade) isValidAnswer = false;
        if(!isValidClass) isValidAnswer = false;
        if(!isValidNumber) isValidAnswer = false;

        if(!isValidAnswer) {
            Toast.makeText(CreateAccountActivity.this, R.string.toast_createAccount_thereIsError, Toast.LENGTH_LONG);
            return;
        }

        EditText mailView = findViewById(R.id.editText_createAccount_mail);
        EditText passView = findViewById(R.id.editText_createAccount_pass);
        String grade = ((EditText)findViewById(R.id.editText_createAccount_grade)).getText().toString();
        String clazz = ((EditText)findViewById(R.id.editText_createAccount_class)).getText().toString();
        String number = ((EditText)findViewById(R.id.editText_createAccount_number)).getText().toString();
        String mail = mailView.getText().toString();
        HashedString pass;
        SignupQuestion question = new SignupQuestion(
                clubs, checkCheckedGender(), term,
                sns, motivation, age, motivationHour, motivationMinute,
                rule, dependence
        );

        try {
            pass = HashedString.encrypt(passView.getText().toString());
        } catch(NoSuchAlgorithmException e) {
            KozuZen.createErrorReport(this, e);
            return;
        }

        TentativeRegisterRequest request = new TentativeRegisterRequest(new TentativeRegisterArguments(mail, pass, grade, clazz, number, question));

        PostAccessCallBack callBack = new PostAccessCallBack(request) {
            @Override
            public void onSuccess(DataBasePostResponse response) {
                InternalTentativeAccountManager.register(mail, pass);
                Intent authIntent = new Intent(CreateAccountActivity.this, AuthorizationActivity.class);
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, mail);
                authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, SixNumberCode.CodeType.FOR_CREATE_ACCOUNT);
                CreateAccountActivity.this.startActivity(authIntent);
            }

            @Override
            public void onFailure(@Nullable DataBasePostResponse response) {
                Logger.i(response.getResponseCode() + ", " + response.getResponseMessage());
                Toast.makeText(CreateAccountActivity.this, R.string.notification_message_tentativeReg_failure, Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                CreateAccountActivity.this.startActivity(loginIntent);
            }

            @Override
            public void onTimeOut(DataBasePostResponse response) {
                retry();
                Logger.i(response.getResponseCode() + ", " + response.getResponseMessage());
                Toast.makeText(CreateAccountActivity.this, KozuZen.getInstance().getString(R.string.toast_failure_timeout), Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                CreateAccountActivity.this.startActivity(loginIntent);
            }
        };
        DataBaseAccessor.showLoadFragment(CreateAccountActivity.this, R.id.frame_createAccount_fragmentFrame);
        DataBaseAccessor.sendPostRequest(request, callBack);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //各種動的設定
        prepareCheckBoxGroup(
                findViewById(R.id.linear_createAccount_club), SignupQuestion.Club.clubNames(),
                SignupQuestion.Club.NONE.getClubName(), this.noneClubListener, this.otherClubListener
        );
        prepareCheckBoxGroup(
                findViewById(R.id.linear_createAccount_sns), SignupQuestion.SNS.SNSNames(),
                SignupQuestion.SNS.NONE.getSNSName(), this.noneSNSListener, this.otherSNSListener
        );
        prepareMotivationHourMinute();
        prepareAge();
        TextView policyLinkView = findViewById(R.id.textView_createAccount_link);
        policyLinkView.setText(Secrets.PRIVACY_POLICY_URL);

        //登録ボタンイベント設定
        Button enterButton = findViewById(R.id.button_createAccount_enter);
        enterButton.setOnClickListener(this.whenEnterClicked);

        //未入力時などのエラー実装
        EditText term = findViewById(R.id.editText_createAccount_term);
        EditText mail = findViewById(R.id.editText_createAccount_mail);
        EditText pass = findViewById(R.id.editText_createAccount_pass);
        EditText passCheck = findViewById(R.id.editText_createAccount_passCheck);
        EditText gradeEditText = findViewById(R.id.editText_createAccount_grade);
        EditText classEditText = findViewById(R.id.editText_createAccount_class);
        EditText numberEditText = findViewById(R.id.editText_createAccount_number);
        pass.setCustomSelectionActionModeCallback(this.actionModeCallback);
        passCheck.setCustomSelectionActionModeCallback(this.actionModeCallback);
        term.addTextChangedListener(this.whenTermInput);
        mail.addTextChangedListener(this.whenMailInput);
        pass.addTextChangedListener(this.whenPassInput);
        passCheck.addTextChangedListener(this.whenPassCheckInput);
        gradeEditText.addTextChangedListener(this.whenGradeCheckInput);
        classEditText.addTextChangedListener(this.whenClassCheckInput);
        numberEditText.addTextChangedListener(this.whenNumberCheckInput);
    }

    /**
     * <p>チェックボックスでの複数選択形式のグループを作る。</p>
     * <p>{@code parentLinear}の子要素として作成され、横列の数は{@code ITEMS_PER_HORIZONAL}に依存する。</p>
     * @param parentVerticalLinear グループが乗っかる親となるLinearLayout。orientationはverticalである必要がある。
     * @param optionTitles チェックボックスのすべての選択肢。
     * @param noneOptionTitle [なし][選択しない]のような、ほかの選択肢と同時に選択できない選択肢。
     * @param noneListener {@code noneOptionTitle}のチェックボックスを選択した際の所作。
     * @param otherListener {@code noneOptionTitle}以外のチェックボックスを選択した際の所作。
     */
    private void prepareCheckBoxGroup(LinearLayout parentVerticalLinear, String[] optionTitles, String noneOptionTitle,
                                      CompoundButton.OnCheckedChangeListener noneListener,
                                      CompoundButton.OnCheckedChangeListener otherListener) {
        LinearLayout horizonalLinear = new LinearLayout(this);
        //チェックボックスを配置
        for(int i = 0; i < optionTitles.length; i++) {
            CompoundButton.OnCheckedChangeListener checkListener = optionTitles[i].equals(noneOptionTitle)
                    ? noneListener : otherListener;
            CheckBox box = new CheckBox(this);
            box.setText(optionTitles[i]);
            box.setOnCheckedChangeListener(checkListener);
            box.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.color_text_title, getTheme())));
            horizonalLinear.addView(box);
            if(((i + 1) % ITEMS_PER_HORIZONAL) == 0) {
                parentVerticalLinear.addView(horizonalLinear);
                horizonalLinear = new LinearLayout(this);
            }
        }
    }

    private void prepareAge() {
        //スマホ持ち始めの年齢の選択肢
        Spinner spinner = findViewById(R.id.spinner_createAccount_age);
        List<String> levelNames = new ArrayList<>();
        for(SignupQuestion.AgeLevel l : SignupQuestion.AgeLevel.values()) {
            levelNames.add(l.getLevelName());
        }
        spinner.setAdapter(new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, levelNames)
        );
    }

    private void prepareMotivationHourMinute() {
        //「どれくらいSNS時間減らしたいか」のスピナー設定
        Spinner hourSpinner = findViewById(R.id.spinner_createAccount_motivation_hour);
        List<Integer> hours = new ArrayList<>();
        Spinner minuteSpinner = findViewById(R.id.spinner_createAccount_motivation_minute);
        List<Integer> minutes = new ArrayList<>();
        for(int hour = 0; hour <= 12; hour++) {
            hours.add(hour);
            if((hour * 10) < 60) {
                minutes.add(hour * 10);
            }
        }
        hourSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hours));
        minuteSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, minutes));
    }

    private List<SignupQuestion.Club> checkCheckedClub() {
        TextView title = findViewById(R.id.textView_club_title);
        title.setError(getString(R.string.text_createAccount_warn_club));
        List<SignupQuestion.Club> clubs = new ArrayList<>();
        LinearLayout clubParent = findViewById(R.id.linear_createAccount_club);
        for(int i = 0; i < clubParent.getChildCount(); i++) {
            LinearLayout childLinear = (LinearLayout)clubParent.getChildAt(i);
            for(int child = 0; child < ITEMS_PER_HORIZONAL; child++) {
                CheckBox box = (CheckBox)childLinear.getChildAt(child);
                if(box.isChecked()) {
                    clubs.add(SignupQuestion.Club.from(box.getText().toString()));
                    title.setError(null);
                }
            }
        }
        return clubs;
    }

    private SignupQuestion.Gender checkCheckedGender() {
        RadioGroup genderParent = findViewById(R.id.radioGroup_createAccount_gender);
        if(genderParent.getCheckedRadioButtonId() == -1) { //チェックがなければ
            return SignupQuestion.Gender.NO_ANSWER;
        }
        RadioButton selected = genderParent.findViewById(genderParent.getCheckedRadioButtonId());
        return SignupQuestion.Gender.from(selected.getText().toString());
    }

    private List<SignupQuestion.SNS> checkCheckedSNS() {
        TextView title = findViewById(R.id.textView_sns_title);
        title.setError(getString(R.string.text_createAccount_warn_sns));
        LinearLayout snsParent = findViewById(R.id.linear_createAccount_sns);
        List<SignupQuestion.SNS> sns = new ArrayList<>();
        for(int i = 0; i < snsParent.getChildCount(); i++) {
            LinearLayout childLinear = (LinearLayout)snsParent.getChildAt(i);
            for(int child = 0; child < ITEMS_PER_HORIZONAL; child++) {
                CheckBox box = (CheckBox)childLinear.getChildAt(child);
                if(box.isChecked()) {
                    sns.add(SignupQuestion.SNS.from(box.getText().toString()));
                    title.setError(null);
                }
            }
        }
        return sns;
    }

    private SignupQuestion.AgeLevel checkCheckedAge() {
        TextView title = findViewById(R.id.textView_age_title);
        title.setError(getString(R.string.text_createAccount_warn_age));
        Spinner s = findViewById(R.id.spinner_createAccount_age);
        SignupQuestion.AgeLevel age = SignupQuestion.AgeLevel.from((String)s.getSelectedItem());
        if(age != null) {
            if(!age.equals(SignupQuestion.AgeLevel.NON_SELECTED)) {
                title.setError(null);
            }
        }
        return age;
    }

    private SignupQuestion.MotivationLevel checkMotivationLevel() {
        TextView title = findViewById(R.id.textView_motivation_title);
        title.setError(getString(R.string.text_createAccount_warn_motivation));
        RadioGroup motivationParent = findViewById(R.id.radioGroup_createAccount_motivation);
        if(motivationParent.getCheckedRadioButtonId() == -1) {
            return null;
        }
        RadioButton b = motivationParent.findViewById(motivationParent.getCheckedRadioButtonId());
        title.setError(null);
        return SignupQuestion.MotivationLevel.from(b.getText().toString());
    }

    private int checkMotivationHour() {
        Spinner motivationHour = findViewById(R.id.spinner_createAccount_motivation_hour);
        return (int)motivationHour.getSelectedItem();
    }

    private int checkMotivationMinute() {
        Spinner motivationMinute = findViewById(R.id.spinner_createAccount_motivation_minute);
        return (int)motivationMinute.getSelectedItem();
    }

    private SignupQuestion.DependenceLevel checkDependenceLevel() {
        TextView title = findViewById(R.id.textView_dependence_title);
        title.setError(getString(R.string.text_createAccount_warn_dependence));
        RadioGroup dependenceSNS = findViewById(R.id.radioGroup_createAccount_dependenceSNS);
        if(dependenceSNS.getCheckedRadioButtonId() == -1) {
            return null;
        }
        RadioButton b = dependenceSNS.findViewById(dependenceSNS.getCheckedRadioButtonId());
        title.setError(null);
        return SignupQuestion.DependenceLevel.from(b.getText().toString());
    }

    private SignupQuestion.Rule checkRule() {
        TextView title = findViewById(R.id.textView_rule_title);
        title.setError(getString(R.string.text_createAccount_warn_rule));
        RadioGroup rule = findViewById(R.id.radioGroup_createAccount_rule);
        if(rule.getCheckedRadioButtonId() == -1) {
            return null;
        }
        RadioButton b = rule.findViewById(rule.getCheckedRadioButtonId());
        title.setError(null);
        return SignupQuestion.Rule.from(b.getText().toString());
    }

    private Integer checkTerm() {
        EditText termEditText = findViewById(R.id.editText_createAccount_term);
        Integer term;
        try {
            term = Integer.parseInt(termEditText.getText().toString());
        } catch(NumberFormatException e) {
            term = null;
        }
        return term;
    }

    private boolean checkMailAddress() {
        EditText mailEditText = findViewById(R.id.editText_createAccount_mail);
        return (mailEditText.getError() == null);
    }

    private boolean checkPassword() {
        EditText passwordEditText = findViewById(R.id.editText_createAccount_pass);
        return (passwordEditText.getError() == null);
    }

    private boolean checkPasswordCheck() {
        EditText passwordCheckEditText = findViewById(R.id.editText_createAccount_passCheck);
        return (passwordCheckEditText.getError() == null);
    }

    private boolean checkReadPolicy() {
        CheckBox readPolicy = findViewById(R.id.check_createAccount_readPolicy);
        boolean isChecked = readPolicy.isChecked();
        readPolicy.setError(isChecked
                ? null : getString(R.string.text_createAccount_warn_policy));
        return isChecked;
    }

    private boolean checkGrade() {
        EditText gradeEditText = findViewById(R.id.editText_createAccount_grade);
        return (gradeEditText.getError() == null);
    }

    private boolean checkClass() {
        EditText classEditText = findViewById(R.id.editText_createAccount_class);
        return (classEditText.getError() == null);
    }

    private boolean checkNumber() {
        EditText numberEditText = findViewById(R.id.editText_createAccount_number);
        return (numberEditText.getError() == null);
    }
}