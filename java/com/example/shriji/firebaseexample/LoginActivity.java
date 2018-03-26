package com.example.shriji.firebaseexample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {


    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edtPassword)
    EditText edtPassword;
    @BindView(R.id.btnSignin)
    Button btnSignin;
    @BindView(R.id.btnCreate)
    Button btnCreate;
    @BindView(R.id.txtUserStatus)
    TextView txtUserStatus;
    @BindView(R.id.btnSignOut)
    Button btnSignOut;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String TAG = "Firebase";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Sign In: " + user.getUid());
                } else {
                    Log.d(TAG, "Currently Sign Out");
                }

            }
        };
        updateStatus();
    }


    @Override
    public void onStart() {
        super.onStart();
        // add auth listener
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }


    }

    @OnClick({R.id.btnSignin, R.id.btnCreate, R.id.btnSignOut})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSignin:
                signUserIn();
                break;
            case R.id.btnCreate:
                createUserAccount();
                break;
            case R.id.btnSignOut:
                signUserOut();
                break;
        }
    }

    private void updateStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            txtUserStatus.setText("Signed In: " + user.getEmail());
        } else {
            txtUserStatus.setText("Sign Out");
        }
    }

    private void updateStatus(String stat){
        txtUserStatus.setText(stat);
    }

    private boolean checkFormFields() {
        String email, password;
        email = edtEmail.getText().toString();
        password = edtPassword.getText().toString();

        if (email.isEmpty()) {
            edtEmail.setError("Email required");
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError("PassWord required");
            return false;
        }

        return true;
    }


    private void createUserAccount() {
        if (!checkFormFields())
            return;

        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "User Was Created", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(LoginActivity.this, "Account Creation Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof FirebaseAuthUserCollisionException){
                    updateStatus("This email is already in use");
                }else {
                    updateStatus(e.getLocalizedMessage());
                }
            }
        });
    }

    private void signUserIn() {
        if (!checkFormFields())
            return;

        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "User Sign In", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(LoginActivity.this, "Sign In failed", Toast.LENGTH_SHORT).show();
                        }
                        updateStatus();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    updateStatus("Invalid Password");
                }else if(e instanceof FirebaseAuthInvalidUserException){
                    updateStatus("No Account With this email");
                }else {
                    updateStatus(e.getLocalizedMessage());
                }
            }
        });

    }

    private void signUserOut() {
        mAuth.signOut();
        updateStatus();
    }


}
