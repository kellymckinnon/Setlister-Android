package me.kellymckinnon.setlister;

import android.app.Application;
import android.content.Context;
import androidx.test.runner.AndroidJUnitRunner;

/**
 * This is necessary to use a TestApplication during testing, which allows mocking
 * the Retrofit calls with MockTestRunner.
 */
public class SetlisterTestRunner extends AndroidJUnitRunner {

  @Override
  public Application newApplication(ClassLoader cl, String className, Context context)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {

    return super.newApplication(cl, TestApplication.class.getName(), context);
  }
}