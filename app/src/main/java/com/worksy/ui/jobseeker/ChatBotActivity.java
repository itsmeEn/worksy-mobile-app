package com.worksy.ui.jobseeker;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.worksy.R;
import com.worksy.data.model.Message;
import com.worksy.ui.adapter.MessageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText message_text_text;
    ImageView send_btn;
    List<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot_job_seeker);

        //====================================
        message_text_text = findViewById(R.id.message_text_text);
        send_btn = findViewById(R.id.send_btn);
        recyclerView = findViewById(R.id.recyclerView);

        // Create Layout behaves and set it in recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //====================================

        //====================================
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        //====================================

        send_btn.setOnClickListener(view -> {
            String question = message_text_text.getText().toString().trim();
            addToChat(question,Message.SEND_BY_ME);
            message_text_text.setText("");
            callAPI(question);
        });

    } // OnCreate Method End Here ================

    void addToChat (String message, String sendBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sendBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    } // addToChat End Here =====================

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SEND_BY_BOT);
    } // addResponse End Here =======

    void callAPI(String question){
        // okhttp
        messageList.add(new Message("Typing...", Message.SEND_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            // Constructing request body for Gemini API
            JSONObject parts = new JSONObject();
            parts.put("text", question);
            JSONArray partsArray = new JSONArray();
            partsArray.put(parts);
            JSONObject contents = new JSONObject();
            contents.put("parts", partsArray);
            JSONArray contentsArray = new JSONArray();
            contentsArray.put(contents);
            jsonBody.put("contents", contentsArray);

            // Gemini API doesn't use max_tokens, temperature in the same way as OpenAI completions
            // You might need to explore Gemini specific parameters if needed, but for basic text generation, this should be sufficient.

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        // Append API key to the URL for Gemini
        Request request = new Request.Builder()
                .url(API.API_URL + API.API)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        // Parsing response for Gemini API
                        JSONArray candidates = jsonObject.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject firstCandidate = candidates.getJSONObject(0);
                            JSONObject content = firstCandidate.getJSONObject("content");
                            JSONArray partsArray = content.getJSONArray("parts");
                            if (partsArray.length() > 0) {
                                String result = partsArray.getJSONObject(0).getString("text");
                                addResponse(result.trim());
                            } else {
                                addResponse("Failed to get text from Gemini response.");
                            }
                        } else {
                            addResponse("No candidates found in Gemini response.");
                        }

                    } catch (JSONException e) {
                        addResponse("Failed to parse Gemini response: " + e.getMessage());
                        Log.e("ChatBotActivity", "Error parsing Gemini response", e);
                    }
                } else {
                    // Handle unsuccessful response for Gemini API
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    addResponse("Failed to load response due to "+ response.code() + ": " + errorBody);
                    Log.e("ChatBotActivity", "Gemini API call failed: " + response.code() + ": " + errorBody);
                }

            }
        });

    } // callAPI End Here =============


} // Public Class End Here =========================
