package com.wasmo.accounts.emails

import com.wasmo.framework.writeHtml
import com.wasmo.sendemail.EmailMessage
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import okio.Buffer

fun challengeCodeEmailMessage(
  from: String,
  to: String,
  baseUrl: String,
  baseUrlHost: String,
  code: String,
) = EmailMessage(
  from = from,
  to = to,
  subject = "Sign in to $baseUrlHost with code $code",
  html = Buffer().run {
    writeHtml {
      body {
        div {
          attributes["style"] =
            """
            |background-color: #ffffff;
            |color: #272727;
            |font-family: sans-serif;
            |""".trimMargin()

          p {
            attributes["style"] = "font-size: 16px;"
            text("Here’s your code to sign in to $baseUrlHost:")
          }

          h1 {
            attributes["style"] = """
              |margin: 0 20px;
              |padding: 10px;
              |border-radius: 10px;
              |background-color: #ebebeb;
              |text-align: center;
              |letter-spacing: 5px;
              """.trimMargin()
            text(code)
          }

          p {
            attributes["style"] = "font-size: 12px; color: #4e4e4e;"
            text("If you didn’t request this code, you can ignore this email.")
          }

          p {
            attributes["style"] = "font-size: 12px; color: #4e4e4e;"
            text("This email was sent by ")

            a(href = baseUrl) {
              attributes["style"] = "color: #737373;"
              text(baseUrlHost)
            }

            text(" to ")
            text(to)
          }
        }
      }
    }
    readUtf8()
  },
)
