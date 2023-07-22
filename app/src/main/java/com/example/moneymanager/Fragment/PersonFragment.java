package com.example.moneymanager.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moneymanager.ExpenseDB.ExpenseDatabase;
import com.example.moneymanager.MainActivity;
import com.example.moneymanager.R;

public class PersonFragment extends Fragment {
    private Button LogoutButton;
    private Button SaveButton;
    private EditText NameEdit;
    private EditText EmailEdit;

    public PersonFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static PersonFragment newInstance(String param1, String param2) {
        PersonFragment fragment = new PersonFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person, container, false);

        LogoutButton = view.findViewById(R.id.LogoutButton);
        SaveButton = view.findViewById(R.id.buttonSave);
        NameEdit = view.findViewById(R.id.editTextName);
        EmailEdit = view.findViewById(R.id.editTextTextEmailAddress);

        // 从 SharedPreferences 中加载数据
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedName = sharedPreferences.getString("name", "");
        String savedEmail = sharedPreferences.getString("email", "");

        // 将数据设置回 EditText
        NameEdit.setText(savedName);
        EmailEdit.setText(savedEmail);

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = NameEdit.getText().toString();
                String email = EmailEdit.getText().toString();

                // 在这里执行保存数据的操作，例如将数据保存到 SharedPreferences 或数据库中
                // 根据您的需求选择合适的数据存储机制
                // 示例：使用 SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (!name.isEmpty()){
                    editor.putString("name",name);
                    editor.apply();
                    // 提示用户数据已保存
                    Toast.makeText(getActivity(), "数据已保存", Toast.LENGTH_SHORT).show();
                }
                if (!email.isEmpty()){
                    editor.putString("email",email);
                    editor.apply();
                    // 提示用户数据已保存
                    Toast.makeText(getActivity(), "数据已保存", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 删除 SharedPreferences 中的所有数据
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // 删除 Room 数据库中的所有数据
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ExpenseDatabase expenseDatabase = ExpenseDatabase.getInstance(requireContext());
                        expenseDatabase.clearAllTables();
                    }
                });

                startActivity(new Intent(requireContext(), MainActivity.class));
                // 提示用户数据已删除
                Toast.makeText(getActivity(), "数据已删除", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}