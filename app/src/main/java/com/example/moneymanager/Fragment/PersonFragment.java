package com.example.moneymanager.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
                // 创建确认对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("确认删除帐号");
                builder.setMessage("确定要删除帐号吗？此操作将删除所有数据并无法撤消。");

                // 添加确认按钮
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 删除 SharedPreferences 中的所有数据
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        // 删除 Room 数据库中的所有数据
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ExpenseDatabase expenseDatabase = ExpenseDatabase.getInstance(requireContext());
                                expenseDatabase.clearAllTables();
                            }
                        }).start();

                        // 提示用户数据已删除
                        Toast.makeText(getActivity(), "数据已删除", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(requireContext(), MainActivity.class));
                        getActivity().finish();
                    }
                });

                // 添加取消按钮
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 用户取消操作，无需执行任何操作
                    }
                });

                // 显示对话框
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return view;
    }
}