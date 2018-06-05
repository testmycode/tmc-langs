import QtQuick 2.6
import QtTest 1.0
import QtQuick.Window 2.2

Item {
    id: container
    width: 400
    height: 400

    TestCase {
        id: clickTest
        name: "tst_Clicker"
        when: windowShown

        SignalSpy {
            id: spy
        }

        function quickPOINT(function_name, point) {
            console.info("TMC:" + function_name + "." + point);
        }

        function test_click() {
            quickPOINT("tst_Clicker::test_click", "test_point");
            var component = Qt.createComponent("ClickerTest.qml")
            compare(component.status, Component.Ready)
            var clicker = component.createObject(container);

            verify(clicker !== null, "clicker created is null")
            waitForRendering(clicker)

            clicker.forceActiveFocus()

            spy.target = clicker
            spy.signalName = "clickChanged"

            compare(spy.count, 0)
            compare(clicker.click, 0)
            mouseClick(clicker, 100, 100, Qt.LeftButton)
            compare(spy.count, 1)
            compare(clicker.click, 1)

            clicker.destroy()
        }

        function test_text() {
            quickPOINT("tst_Clicker::test_text", "test_point2");
            var component = Qt.createComponent("ClickerTest.qml")
            compare(component.status, Component.Ready)
            var clicker = component.createObject(container);

            verify(clicker !== null, "clicker created is null")
            waitForRendering(clicker)

            clicker.forceActiveFocus()

            mouseClick(clicker, 100, 100, Qt.LeftButton)
            compare(clicker.click, 1)
            compare(clicker.status.text, "Clicks clicked: 1")

            clicker.destroy()
        }
    }

}
