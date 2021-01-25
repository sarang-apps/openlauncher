package com.benny.openlauncher.activity.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.benny.openlauncher.R
import com.benny.openlauncher.activity.ColorActivity

/**
 * Simplest activity which just prompt the user for password
 * If true password, just returns Activity.RESULT_OK
 * note that - back press is disabled.
 */
class SecurityPwdCheckActivity : ColorActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_security_pwd_check)

        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)

        password.apply {
            afterTextChanged {
                login.isEnabled = password.text.toString().isNotEmpty()
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        tryLogin(password.text.toString())
                }
                false
            }

            login.setOnClickListener {
                tryLogin(password.text.toString())
            }
        }
    }

    private fun tryLogin(password: String) {
        val loading = findViewById<ProgressBar>(R.id.loading)
        loading.visibility = View.VISIBLE

        if ("abc123" == password) {
            val data = Intent()
            setResult(Activity.RESULT_OK, data)
            finish()
        } else {
            Toast.makeText(this, "Incorrect password. please try again", Toast.LENGTH_SHORT).show()
            loading.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        // no-op as we want to just disable it
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}