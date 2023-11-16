package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {

    String ed_tx_em, pass;
    private final int Pick_image = 1;
    ImageView imgUser;
    ArrayList<String> arrayAchievements = new ArrayList<>();
    ArrayList<String> arrayProjects = new ArrayList<>();
    ArrayList<String> arraySkills = new ArrayList<>();
    EditText edit_text_email, pass_user;
    TextView projectSize, upSize, name;
    Button btn, PickImage ;

    private final String serverUrl = "http://192.168.0.114:8080/";
    private String token;
    String user_name, user_surname;

    // Создется объект класса OkHttpClien
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        btn = findViewById(R.id.buttonAuth);
        edit_text_email = findViewById(R.id.editTextEmail);
        pass_user = findViewById(R.id.editTextTextPassword);

        //Связываемся с нашим ImageView:
        //Настраиваем для нее обработчик нажатий OnClickListener:
    }

    public void onClickAuth(View view) {
        System.out.println("Start");
        ed_tx_em = String.valueOf(edit_text_email.getText());
        pass = String.valueOf(pass_user.getText());

        // Аутентификация пользователя
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonRequest  = new JSONObject();
        try {
            jsonRequest.put("email", ed_tx_em);
            jsonRequest.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);
        Request.Builder requestBuilder = new Request.Builder().url(serverUrl + "user/sign-in").post(body);
        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            // Если сервер не ответил
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Blyat, it isn't work");
                e.printStackTrace();
            }

            // Если сервер ответил
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("So, it's work");
                // Проверка запроса на успешность
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Запрос к серверу не был успешен: " +
                                response.code() + " " + response.message());
                    }

                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        System.out.println(responseHeaders.name(i) + ": "
                                + responseHeaders.value(i));
                    }
                    // Вывод тела ответа
                    HashMap response_json = new ObjectMapper().readValue(responseBody.string(), HashMap.class);
                    HashMap user = (HashMap) response_json.get("user");
                    token = (String) response_json.get("token");
                    user_name = (String) user.get("name");
                    user_surname = (String) user.get("surname");
                    System.out.println(user + "\n" + token);

                    // Получение резюме пользователя
                    Request request = new Request.Builder()
                            .header("Authorization", "Bearer " + token)
                            .url(serverUrl + "resume/get-resume")
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        // Если сервер не ответил
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        // Если сервер ответил
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            // Проверка запроса на успешность
                            try (ResponseBody responseBody = response.body()) {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Запрос к серверу не был успешен: " +
                                            response.code() + " " + response.message());
                                }

                                Headers responseHeaders = response.headers();
                                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                                    System.out.println(responseHeaders.name(i) + ": "
                                            + responseHeaders.value(i));
                                }
                                // Вывод тела запроса
                                HashMap response_json = new ObjectMapper().readValue(responseBody.string(), HashMap.class);
                                HashMap resume = (HashMap) response_json.get("resume");
                                arraySkills.addAll((Collection<? extends String>) resume.get("skills"));
                                arrayAchievements.addAll((Collection<? extends String>) resume.get("achievements"));
                                arrayProjects.addAll((Collection<? extends String>) resume.get("projects"));
                                System.out.println(response_json);
                                renderMainScreen();
                            }
                        }
                    });
                }
            }
        });
        System.out.println("End");
    }

    // Формирование и отрисовка главного экрана
    private void renderMainScreen() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setContentView(R.layout.home);
                name = findViewById(R.id.nameUser);
                name.setText(user_surname + " " + user_name);
                projectSize = findViewById(R.id.textViewProject);
                upSize = findViewById(R.id.textViewUp);
                upSize.setText("Достижений:\n" + arrayAchievements.size());
                projectSize.setText("Проектов:\n" + arrayProjects.size());
                Button buttonUp = findViewById(R.id.buttonUp);
                Button buttonProject = findViewById(R.id.buttonProject);
                Button buttonProjectText = findViewById(R.id.buttonProjectText);
                Button buttonUpText = findViewById(R.id.buttonUpText);
                Button buttonSkills = findViewById(R.id.buttonSkills);
                Button buttonSkillsText = findViewById(R.id.buttonSkillsText);
                Button buttonResume = findViewById(R.id.buttonResume);
                Button buttonD = findViewById(R.id.buttonD);
                ImageButton buttonReg = findViewById(R.id.buttonReg);
                imgUser = findViewById(R.id.roundedImageViewUser);

                //Связываемся с нашей кнопкой Button:
                PickImage = findViewById(R.id.buttonUser);
                PickImage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        //Вызываем стандартную галерею для выбора изображения с помощью Intent.ACTION_PICK:
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        //Тип получаемых объектов - image:
                        photoPickerIntent.setType("image/*");
                        //Запускаем переход с ожиданием обратного результата в виде информации об изображении:
                        startActivityForResult(photoPickerIntent, Pick_image);
                    }
                });





// редакт им
                buttonReg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogReg();
                    }
                });


// для достижений
                buttonUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogUpPlus();
                    }
                });
                buttonUpText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogUpText();
                    }
                });

// для проектов
                buttonProject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogProjectPlus();
                    }
                });
                buttonProjectText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogProjectText();
                    }
                });

// для скилов

                buttonSkills.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogSkillsPlus();
                    }
                });
                buttonSkillsText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogSkillsText();
                    }
                });


// для резюмехи

                buttonResume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialogResume();
                    }
                });


// download блять

                buttonD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        download();
                    }
                });

            }
        });
    }


    private void download(){
        String resume = "Достижения:\n\t" + String.join("\n\t", arrayAchievements) + "\n" + "Проекты:\n\t" + String.join("\n\t", arrayProjects) + "\n" + "Навыки:\n\t" + String.join("\n\t", arraySkills);
        try {
            FileOutputStream fo = openFileOutput("resume.docx", MODE_APPEND);
            fo.write(resume.getBytes(StandardCharsets.UTF_8));
            fo.close();
            Toast.makeText(MainActivity.this, "Ваше резюме сохранено", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // редакт им
    private void openDialogReg() {
        Dialog dialogReg = new Dialog(this);
        dialogReg.setContentView(R.layout.dialog_up_plus);

        EditText userInput = dialogReg.findViewById(R.id.user_input);

        Button saveButton = dialogReg.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
            @Override
            public void onClick(View v) {
                String inputText = userInput.getText().toString();  // получаем введённый текст
                if(!inputText.equals(""))
                    name.setText(inputText);
                upSize.setText("Достижений:\n" + arrayAchievements.size());
                dialogReg.dismiss(); // закрываем диалоговое окно
            }
        });

        dialogReg.show (); // отображаем диалоговое окно
    }


    // Обновление данных о резюме
    private void updateResume() {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        org.json.simple.JSONObject jsonRequest = new org.json.simple.JSONObject();
        try {
            jsonRequest.put("skills", arraySkills);
            jsonRequest.put("achievements", arrayAchievements);
            jsonRequest.put("projects", arrayProjects);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(serverUrl + "resume/edit-resume")
                .put(requestBody);
        Request request = requestBuilder.build();
        System.out.println("Погнали");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println("БЛЯЯЯЯЯЯЯЯ");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Запрос к серверу не был успешен: " +
                                response.code() + " " + response.message());
                    }

                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        System.out.println(responseHeaders.name(i) + ": "
                                + responseHeaders.value(i));
                    }
                    // Вывод тела ответа
                    System.out.println("Имба");
                    System.out.println(responseBody.toString());
                    renderMainScreen();
                }
            }
        });
        }




    // для достижений
    private void openDialogUpPlus() {
        Dialog dialogUpPlus = new Dialog(this);
        dialogUpPlus.setContentView(R.layout.dialog_up_plus);

        EditText userInput = dialogUpPlus.findViewById(R.id.user_input);

        Button saveButton = dialogUpPlus.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
            @Override
            public void onClick(View v) {
                String inputText = userInput.getText().toString();  // получаем введённый текст
                if(!inputText.equals(""))
                    arrayAchievements.add(inputText);
                upSize.setText("Достижений:\n" + arrayAchievements.size());
                updateResume(); // Отправляем изменения на сервер
                dialogUpPlus.dismiss(); // закрываем диалоговое окно
            }
        });

        dialogUpPlus.show (); // отображаем диалоговое окно
        }
//#####################################


    private void openDialogUpText() {
        Dialog dialogUpText = new Dialog(this);
        dialogUpText.setContentView(R.layout.dialog_up_text);
        TextView textUp = dialogUpText.findViewById(R.id.textUp);
        textUp.setText("Достижения:\n\t" + String.join("\n\t", arrayAchievements));
        Button ok = dialogUpText.findViewById(R.id.save_button);
        ok.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
            @Override
            public void onClick(View v) {
                dialogUpText.dismiss(); // закрываем диалоговое окно
            }
        });

        dialogUpText.show(); // отображаем диалоговое окно
    }

// для проектов

    private void openDialogProjectPlus() {
        Dialog dialogProjectPlus = new Dialog(this);
        dialogProjectPlus.setContentView(R.layout.dialog_up_plus);

        EditText userInput = dialogProjectPlus.findViewById(R.id.user_input);

        Button saveButton = dialogProjectPlus.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
            @Override
            public void onClick(View v) {
                String inputText = userInput.getText().toString();  // получаем введённый текст
                if (!inputText.equals(""))
                    arrayProjects.add(inputText);
                projectSize.setText("Проектов:\n" + arrayProjects.size());
                updateResume(); // Отправляем изменения на сервер
                dialogProjectPlus.dismiss(); // закрываем диалоговое окно
            }
        });

        dialogProjectPlus.show(); // отображаем диалоговое окно
    }


//        ##############################

    private void openDialogProjectText() {
        Dialog dialogProjectText = new Dialog(this);
        dialogProjectText.setContentView(R.layout.dialog_up_text);
        TextView textUp = dialogProjectText.findViewById(R.id.textUp);
        textUp.setText("Проекты:\n\t" + String.join("\n\t", arrayProjects));
        Button ok = dialogProjectText.findViewById(R.id.save_button);
        ok.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
            @Override
            public void onClick(View v) {
                dialogProjectText.dismiss(); // закрываем диалоговое окно
            }
        });

        dialogProjectText.show(); // отображаем диалоговое окно
    }

//  для скилов

    private void openDialogSkillsPlus() {
        Dialog dialogSkillsPlus = new Dialog(this);
        dialogSkillsPlus.setContentView(R.layout.dialog_up_plus);

        EditText userInput = dialogSkillsPlus.findViewById(R.id.user_input);

        Button saveButton = dialogSkillsPlus.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
            @Override
            public void onClick(View v) {
                String inputText = userInput.getText().toString();  // получаем введённый текст
                if (!inputText.equals(""))
                    arraySkills.add(inputText);
                updateResume(); // Отправляем изменения на сервер
                dialogSkillsPlus.dismiss(); // закрываем диалоговое окно
            }
        });

        dialogSkillsPlus.show(); // отображаем диалоговое окно
    }





//    ########################################


        private void openDialogSkillsText() {
            Dialog dialogSkillsText = new Dialog(this);
            dialogSkillsText.setContentView(R.layout.dialog_up_text);
            TextView textUp = dialogSkillsText.findViewById(R.id.textUp);
            textUp.setText("Навыки:\n\t" + String.join("\n\t", arraySkills));
            Button ok = dialogSkillsText.findViewById(R.id.save_button);
            ok.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
                @Override
                public void onClick(View v) {
                    dialogSkillsText.dismiss(); // закрываем диалоговое окно
                }
            });

            dialogSkillsText.show(); // отображаем диалоговое окно
        }




//        резюме




    private void openDialogResume() {
        Dialog dialogResume = new Dialog(this);
        dialogResume.setContentView(R.layout.dialog_up_text);
        TextView textUp = dialogResume.findViewById(R.id.textUp);
        textUp.setText("Достижения:\n\t" + String.join("\n\t", arrayAchievements) + "\n" + "Проекты:\n\t" + String.join("\n\t", arrayProjects) + "\n" + "Навыки:\n\t" + String.join("\n\t", arraySkills));
        Button ok = dialogResume.findViewById(R.id.save_button);
        ok.setOnClickListener(new View.OnClickListener() {  // при нажатии на кнопку сохраняем текст в переменную textUser
            @Override
            public void onClick(View v) {
                dialogResume.dismiss(); // закрываем диалоговое окно
            }
        });

        dialogResume.show(); // отображаем диалоговое окно

    }





    //Обрабатываем результат выбора в галерее:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case Pick_image:
                if(resultCode == RESULT_OK){
                    try {

                        //Получаем URI изображения, преобразуем его в Bitmap
                        //объект и отображаем в элементе ImageView нашего интерфейса:
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imgUser.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
