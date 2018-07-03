package com.example.rajjadon_pc.phoneauth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.TextView  ;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final  String TAG = "phoneLogin";
    private  boolean mVerficationInProgress = false;
    private  String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private  FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

                if (!isConnected(MainActivity.this))
                {
                    buildDailog(MainActivity.this).show();
                }
                else
                {
                    setContentView(R.layout.activity_main);
                }

        final TextView maintext1 = findViewById(R.id.maintext1);
        final EditText phone_no  = findViewById(R.id.phone_no);
        final Button   sendotp   = findViewById(R.id.otpsend_button);
        final TextView maintext2 = findViewById(R.id.maintext2);
        final EditText otpverify_ed  = findViewById(R.id.otpverify_ed);
        final Button   verifyotp   = findViewById(R.id.otpverify_button);


        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                // Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerficationInProgress = false;
                Toast.makeText(MainActivity.this,"Verification Complete", Toast.LENGTH_SHORT).show();

                // if the user already register
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                //Log.w (TAG,"onverificationfailed",e);
                Toast.makeText(MainActivity.this,"Varification failed",Toast.LENGTH_LONG).show();
                if(e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    //invalid request
                    Toast.makeText(MainActivity.this,"invalid phone number",Toast.LENGTH_LONG).show();
                }
                else if(e instanceof FirebaseTooManyRequestsException)
                {

                }
            }

            @Override
            public  void onCodeSent(String VerificationId , PhoneAuthProvider.ForceResendingToken token)
            {
                //Log.d(Tag,"onCodeSent"+ verificationId);
                Toast.makeText(MainActivity.this,"verification code has been sent to your phone",Toast.LENGTH_LONG).show();
                //save verification id and resendind token so we can use them later
                mVerificationId = VerificationId;

                maintext1.setVisibility(View.GONE);
                phone_no.setVisibility(View.GONE);
                sendotp.setVisibility(View.GONE);
                maintext2.setVisibility(View.VISIBLE);
                otpverify_ed.setVisibility(View.VISIBLE);
                verifyotp.setVisibility(View.VISIBLE);


            }
        };
        sendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phone_no.getText().toString(),
                        60,
                        TimeUnit.SECONDS,
                        MainActivity.this,
                        mCallbacks);
            }
        });

        verifyotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpverify_ed.getText().toString());
                // [END verify_with_code]
                signInWithPhoneAuthCredential(credential);
            }
        });



    }

    public boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (mobile != null && networkInfo.isConnectedOrConnecting() || wifi != null && networkInfo.isConnectedOrConnecting())

                return true;
            else
                return  false;
        }
        else
            return  false;
    }

    public AlertDialog.Builder buildDailog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("No InterNet Connection");
        builder.setMessage("Please Turn on the Mobile Data or Wifi");
        builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }

        });
        builder.create();
        builder.show();

        return builder;
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Log.d(TAG, "signInWithCredential:success");
                            startActivity(new Intent(MainActivity.this,Home.class));
                            Toast.makeText(MainActivity.this,"Verification Done",Toast.LENGTH_SHORT).show();
                            // ...
                        }
                        else
                        {
                            // Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                // The verification code entered was invalid
                                Toast.makeText(MainActivity.this,"Invalid Verification",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }



}
