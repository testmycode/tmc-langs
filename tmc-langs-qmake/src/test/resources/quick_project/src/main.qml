import QtQuick 2.0
import QtQuick.Window 2.2

Window {
    id: window
    width: 620
    visible: true
    height: 420
    title: qsTr("Smiley clicker!")

    Clicker {
        clickedType: "Smiley"
        width: 350
        height: 200
        anchors.centerIn: parent
    }

}
