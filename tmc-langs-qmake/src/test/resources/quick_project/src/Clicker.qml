import QtQuick 2.9
import QtQuick.Dialogs 1.1
import QtQuick.Controls 1.0

Item {
    id: clicker
    anchors.fill: parent

    property int click: 0
    property alias status: clickerText
    property string clickedType : "Generic"

    Image {
        id: image
        anchors.fill: parent
        fillMode: Image.PreserveAspectFit
        source: "qrc:/smile.png"

        MouseArea {
            anchors.fill: parent

            onClicked: {
                click++;
            }
        }
    }

    Text {
        anchors.horizontalCenter: parent.horizontalCenter
        id: clickerText
        x: 0
        y: 0
        text: clickedType + "s clicked: " + click;
        color: "brown"
        font.pixelSize: 30
    }
}
