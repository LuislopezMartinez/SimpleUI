import processing.core.PApplet;
import simpleui.android.*;
import simplecore.Core;

public class AndroidSmoke extends PApplet {
    UIButton button;
    UITextField field;
    UIViewManager views;
    UICalendar calendar;
    UISignalMeter signalMeter;
    UISlider slider;
    UITabs tabs;
    chatArea chat;
    Core taskCore;

    public void setup() {
        if (!"Luis lopez martinez".equals(SimpleUI.LIBRARY_AUTHOR)) {
            throw new AssertionError("Unexpected SimpleUI author metadata");
        }
        if (!"MIT".equals(SimpleUI.LIBRARY_LICENSE)) {
            throw new AssertionError("Unexpected SimpleUI license metadata");
        }
        SimpleUI.initUI(this, "SansSerif", 18);
        taskCore = Core.start(this);
        taskCore.setAutomaticRendering(false);
        SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);
        button = new UIButton("send", 20, 20, 160, 48, "Send", 18);
        field = new UITextField("message", 20, 84, 300, 48, "Message", 18);
        views = new UIViewManager("main");
        calendar = new UICalendar("calendar", 20, 150, 360, 240, new UIDate(2026, 7, 22));
        signalMeter = new UISignalMeter("signal", 20, 410, 300, 70, "LoRa");
        signalMeter.setRssi(-92);
        slider = new UISlider("level", 20, 500, 260, 32, 0, 100, 50, 16);
        tabs = new UITabs("tabs", 20, 550, 300, 40, new String[]{"One", "Two"}, 16);
        UIRect secondTab = tabs.getTabBounds(1);
        if (secondTab == null) throw new AssertionError("Missing tab bounds");
        chat = new chatArea("chat", 20, 610, 300, 160);
        chat.addMessage(RIGHT, "Ready", "10:30", true);
        SimpleUI.addUIElement(button);
        SimpleUI.addUIElement(field);
        SimpleUI.addUIElement(calendar);
        SimpleUI.addUIElement(signalMeter);
        SimpleUI.addUIElement(slider);
        SimpleUI.addUIElement(tabs);
        SimpleUI.addUIElement(chat);
    }

    public void draw() {
        SimpleUI.syncHostState();
        SimpleUI.updateUI();
        SimpleUI.drawUI();
    }
}
