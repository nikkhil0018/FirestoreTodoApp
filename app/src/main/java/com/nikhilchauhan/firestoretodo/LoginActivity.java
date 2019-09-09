package com.nikhilchauhan.firestoretodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    EditText phoneText;
    EditText otpText;
    Button sendButton;
    Button verifyButton;
    String phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        phoneText = findViewById(R.id.phone_txtView);
        otpText = findViewById(R.id.otp_txtView);
        sendButton = findViewById(R.id.sendtp_btn);
        verifyButton = findViewById(R.id.submit_btn);


        // When SEND OTP btn is tapped
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneNumber=phoneText.getText().toString();
                if (phoneNumber.isEmpty()) {
                    phoneText.requestFocus();
                }else if (phoneNumber.length() < 10){
                    phoneText.setError("Invalid mobile number");
                    phoneText.requestFocus();
                } else {
                    sendButton.setEnabled(false);
                    phoneNumber = "+91"+phoneText.getText().toString();
                  // progressBar.setVisibility(View.VISIBLE);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            mCallbacks //declared below

                    );

                }
            }
        });

        //When Verify Button is Tapped
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // progressBar.setVisibility(View.VISIBLE);
                String verificationCode = otpText.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential); //declared below

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                otpText.setError("There was some error in verification");
                otpText.requestFocus();

            }

            //After otp is sent to phone
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                //progressBar.setVisibility(View.INVISIBLE);
            }

        };

    }

    //Copied from firebase doc
    private void signInWithPhoneAuthCredential (PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //If Otp is entered is correct
                            FirebaseUser user = task.getResult().getUser();
                            Intent mainIntnet = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(mainIntnet);

                        } else {

                            //If OTP entered is wrong
                            otpText.setError("OTP entered is wrong!");
                            otpText.requestFocus();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }



}
