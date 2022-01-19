package org.avvento.apps.onlineradio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

public class PrayerRequest extends AppCompatActivity {

    private CountryCodePicker ccp;
    private Button SEND_PRAYER_REQUEST;
    private TextInputEditText FULL_NAME,NUMBER,EMAIL,PRAY_FOR,PRAYER_REQUEST;
    private boolean isAllFieldsChecked = false;
    Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_request);

        ccp = findViewById(R.id.ccp);

        initialise();

    }

    private void initialise() {
        ccp = findViewById(R.id.ccp);
        FULL_NAME = findViewById(R.id.textInputEditText_name);
        NUMBER = findViewById(R.id.textInputEditText_number);
        EMAIL = findViewById(R.id.textInputEditText_email);
        PRAY_FOR = findViewById(R.id.textInputEditText_prayFor);
        PRAYER_REQUEST = findViewById(R.id.textInputEditText_message);
        SEND_PRAYER_REQUEST = findViewById(R.id.send_PrayerRequest);

        //on clicking the send button
        SEND_PRAYER_REQUEST.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //method to send email
                sendEmail();

            }
        });
    }

    private void sendEmail() {
        final String email = "avventohomeproductions@gmail.com";
        final String subject = "PRAYER REQUEST";
        String fullName_Sent = FULL_NAME.getText().toString();
        String number_Sent = "+" + ccp.getFullNumber() + NUMBER.getText().toString();
        String email_Sent = EMAIL.getText().toString();
        String prayFor_Sent = PRAY_FOR.getText().toString();
        String prayRequest_Sent = PRAYER_REQUEST.getText().toString();
        isAllFieldsChecked = CheckAllFields();

        String message = "PRAYER REQUEST"+"\n"+"NAME : " + fullName_Sent + "\n" + "MobileNumber : " + number_Sent
                + "\n" + "Email : " + email_Sent + "\n" + "Pray For : " + prayFor_Sent
                + "\n" + "Prayer Request Message : " + prayRequest_Sent + "\n";

        if (isAllFieldsChecked) {
            Intent i = new Intent(PrayerRequest.this, MainActivity.class);
            startActivity(i);

            String mail = "mailto:" + email +
                    "?&subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(mail));
            try {
                startActivity(Intent.createChooser(intent,"Send Email.."));
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(PrayerRequest.this,"Fill in the missing fields",Toast.LENGTH_LONG).show();
        }


    }

    private boolean CheckAllFields() {
        if (FULL_NAME.length() == 0) {
            FULL_NAME.setError("This field is required");
            return false;
        }else if (FULL_NAME.length() > 30) {
        FULL_NAME.setError("Full Name must be maximum 30 characters");
        return false;
    }

        if (NUMBER.length() == 0) {
            NUMBER.setError("This field is required");
            return false;
        } else if (NUMBER.length() < 9 ) {
        NUMBER.setError("Number must be 9-10 characters");
        return false;
    }

        if (EMAIL.length() == 0) {
            EMAIL.setError("Email is required");
            return false;
        } else if (utils.isValidEmail(EMAIL.getText().toString())==false) {
        EMAIL.setError("Email is invalid");
        return false;
    }

        if (PRAY_FOR.length() == 0) {
            PRAY_FOR.setError("Pray For is required");
            return false;
        }

        if (PRAYER_REQUEST.length() == 0) {
            PRAYER_REQUEST.setError("Prayer Request is required");
            return false;
        }

        // after all validation return true.
        return true;
    }
}
