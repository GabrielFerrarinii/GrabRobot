package org.example

import core.GrabRobot

fun main() {

    val robot = GrabRobot.Builder().build()

    robot.pressInImage("img_1.png")
}
