package com.rikcore.nfcreader;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.thekhaeng.pushdownanim.PushDownAnim;


public class MainActivity extends AppCompatActivity implements TextWatcher {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    TextInputLayout textInputLayoutId;
    EditText editTextId;
    TextView textViewNfcStatus;
    Button buttonEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInputLayoutId = findViewById(R.id.textInputId);
        editTextId = findViewById(R.id.editTextId);
        textViewNfcStatus = findViewById(R.id.textViewNfcStatus);

        buttonEnter = findViewById(R.id.buttonEnter);
        buttonEnter.setVisibility(View.GONE);

        editTextId.addTextChangedListener(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            //nfc not support your device.
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        
        PushDownAnim.setPushDownAnimTo(buttonEnter)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "Accès à la réunion", Toast.LENGTH_SHORT).show();
                    }
                });

        IntentFilter filter = new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED");
        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if(nfcAdapter.isEnabled()){
                    textViewNfcStatus.setVisibility(View.GONE);
                } else {
                    textViewNfcStatus.setVisibility(View.VISIBLE);
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent){
        if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            readFromTag(intent, detectedTag);
        }
    }

    public void readFromTag(Intent intent, Tag tag){

        Ndef ndef = Ndef.get(tag);

        try{
            ndef.connect();

            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (messages != null) {
                NdefMessage[] ndefMessages = new NdefMessage[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    ndefMessages[i] = (NdefMessage) messages[i];
                }
                NdefRecord record = ndefMessages[0].getRecords()[0];

                byte[] payload = record.getPayload();

                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";

                //Get the Language Code
                int languageCodeLength = payload[0] & 0077;
                String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

                //Get the Text
                String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                editTextId.setText(text);


                ndef.close();

            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Cannot Read From Tag.", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(editTextId.length() == 12){
            buttonEnter.setVisibility(View.VISIBLE);
        } else {
            buttonEnter.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
