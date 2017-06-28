package com.devfun.cartoon.base;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.devfun.cartoon.model.BaseModel;
import com.devfun.cartoon.model.BaseResponseModel;
import com.devfun.cartoon.model.ErrorModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * *******************************************
 * * Project cartoon                **
 * * Created by Simon on 6/24/2017.           **
 * * Copyright (c) 2017 by DevFun        **
 * * All rights reserved                    **
 * *******************************************
 */

public abstract class BaseActivity<T extends BaseModel>
        extends AppCompatActivity implements Callback<T> {
    protected abstract void onDataResult(T result);

    private List<Call<T>> mCalls = new ArrayList<>();

    protected void request(Call<T> caller) {
        caller.enqueue(this);
        mCalls.add(caller);
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        mCalls.remove(call);
        T object = response.body();
        if (object instanceof BaseResponseModel) {
            if (((BaseResponseModel) object).getError() != null) {
                showError(((BaseResponseModel) object).getError());
            } else {
                onDataResult(object);
            }
        } else {
            try {
                @SuppressWarnings("ConstantConditions")
                String error = new String(response.errorBody().bytes());
                BaseResponseModel fBaseResponseModel = new Gson().fromJson(error, BaseResponseModel.class);
                showError(fBaseResponseModel.getError());
            } catch (IOException e) {
                e.printStackTrace();
                onFailure(call, e);
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        ErrorModel fErrorModel = new ErrorModel();
        fErrorModel.setMessage(t.getMessage());
        showError(fErrorModel);
        mCalls.remove(call);
    }

    protected void showError(ErrorModel errorModel) {
        AlertDialog fAlertDialog = new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(errorModel.getMessage())
                .setPositiveButton("Đóng", null)
                .create();
        fAlertDialog.show();
    }

    @Override
    protected void onDestroy() {
        for (Call<T> call :
                mCalls) {
            call.cancel();
        }
        super.onDestroy();
    }
}
