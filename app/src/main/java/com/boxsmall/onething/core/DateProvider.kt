package com.boxsmall.onething.core

import java.time.LocalDate

fun interface DateProvider {
    fun today(): LocalDate
}

class SystemDateProvider : DateProvider {
    override fun today(): LocalDate = LocalDate.now()
}
