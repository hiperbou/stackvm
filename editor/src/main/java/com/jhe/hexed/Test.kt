package com.jhe.hexed

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import java.util.*
import javax.swing.JFrame

/**
 * Created by IntelliJ IDEA.
 * User: laullon
 * Date: 08-abr-2003
 * Time: 13:16:06
 */
class Test : WindowAdapter() {
    private val win: JFrame

    init {
        val ar: ByteArray
        ar = ByteArray(16 * 16 * 100)
        Arrays.fill(ar, 0.toByte())

        //ByteArrayOutputStream bos=new ByteArrayOutputStream();
        //ObjectOutputStream oos=new ObjectOutputStream(bos);
        //oos.writeObject("dfasnvcxnz.,mvnmc,xznvmcxzmnvcmxzcccbnxz cz hajk vc jbcvj xbnzvc sbj cvxz,bcxjnzbcvjhs avcjz cxmzncvxz ");
        //ar=bos.toByteArray();
        win = JFrame()
        win.contentPane.add(JHexEditor(ar))
        win.addWindowListener(this)
        win.pack()
        win.show()
    }

    override fun windowClosing(e: WindowEvent) {
        System.exit(0)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(arg: Array<String>) {
            Test()
        }
    }
}