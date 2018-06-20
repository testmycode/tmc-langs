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
    }

    ClickerTest {

    }

}
