package core

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import net.sourceforge.tess4j.Tesseract
import org.sikuli.script.Pattern
import org.sikuli.script.Screen
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.KeyEvent.VK_CONTROL
import java.awt.event.KeyEvent.VK_V
import java.math.BigDecimal
import java.math.BigDecimal.ONE

class GrabRobot private constructor(
    private val screen: Screen,
    private val robot: Robot,
    private val teseract: Tesseract
) {

    companion object {
        fun getUrlResource(resource: String): String {
            return this::class.java.classLoader.getResource(resource)?.toURI()?.path ?: throw RuntimeException("Resource not found")
        }
    }

    class Builder {
        private var screen: Screen = Screen()
        private var robot: Robot = Robot()
        private var teseract: Tesseract = Tesseract()

        fun setScreen(screen: Screen) = apply { this.screen = screen }
        fun setRobot(robot: Robot) = apply { this.robot = robot }
        fun setTesseract(teseract: Tesseract) = apply { this.teseract = teseract }

        fun build() = GrabRobot(screen, robot, teseract)
    }

    init {
        teseract.setDatapath(getUrlResource("tessdata-main"))
    }


    fun attachSystem(sytemName: String): WinDef.HWND {
        val hWnd = User32.INSTANCE.FindWindow(null, sytemName) ?: throw RuntimeException("System not found")

        User32.INSTANCE.ShowWindow(hWnd, User32.SW_RESTORE)
        User32.INSTANCE.SetForegroundWindow(hWnd)

        return hWnd
    }

    fun openSystem(filePath: String, awaitTime: Long = 10000): Process? {
        val process = ProcessBuilder(filePath).start()
        Thread.sleep(awaitTime)

        return process
    }


    fun pressKey(key: Int){
        robot.keyPress(key)
        robot.keyRelease(key)
    }


    fun pressTwoKeys(keyOne: Int, keyTwo: Int){
        robot.keyPress(keyOne)
        robot.keyPress(keyTwo)
        robot.keyRelease(keyTwo)
        robot.keyRelease(keyOne)
    }

    /**
     * click on a specific image that is in the resource folder
     *
     * @param nameImageInResources image in resource folder
     * @param timeOut time out to search image
     */
    fun pressInImage(nameImageInResources: String, timeOut: BigDecimal = ONE){
        val imagePattern = Pattern(getUrlResource(nameImageInResources))
        val target = screen.wait(imagePattern, timeOut.toDouble()) ?: throw RuntimeException("Image not found")
        screen.click(target)
    }

    fun pasteText(text: String){
        val stringSelection = java.awt.datatransfer.StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, stringSelection)

        pressTwoKeys(VK_CONTROL, VK_V)
    }

    fun getTextInScreen(sistem: WinDef.HWND): String? {
        val rect = WinDef.RECT()
        User32.INSTANCE.GetWindowRect(sistem, rect)

        val screenRect = Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top)
        val screenCapture = robot.createScreenCapture(screenRect)
        return teseract.doOCR(screenCapture)
    }

    fun awaitSystem(timeOut: Int = 500) {
        Thread.sleep(timeOut.toLong())
    }

}