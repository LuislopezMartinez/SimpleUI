import processing.core.PApplet;
import simpleui.desktop.*;
import simplecore.Core;

public class DesktopSmoke extends PApplet {
    UIButton button;
    UIList list;
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
        SimpleUI.initUI(this, "SansSerif", 16);
        SimpleUI.isWindowsResizeGuardActive();
        taskCore = Core.start(this);
        SimpleUI.setMode(800, 720);
        button = new UIButton("ok", 20, 20, 140, 44, "OK", 16);
        list = new UIList("items", 20, 80, 300, 240, 16);
        views = new UIViewManager("main");
        calendar = new UICalendar("calendar", 340, 80, 260, 220, new UIDate(2026, 7, 22));
        signalMeter = new UISignalMeter("signal", 20, 330, 300, 70, "LoRa");
        signalMeter.setRssi(-92);
        slider = new UISlider("level", 20, 420, 260, 32, 0, 100, 50, 16);
        tabs = new UITabs("tabs", 20, 470, 300, 40, new String[]{"One", "Two"}, 16);
        UIRect secondTab = tabs.getTabBounds(1);
        if (secondTab == null) throw new AssertionError("Missing tab bounds");
        chat = new chatArea("chat", 20, 530, 300, 160);
        chat.addMessage(RIGHT, "Ready", "10:30", true);
        SimpleUI.addUIElement(button);
        SimpleUI.addUIElement(list);
        SimpleUI.addUIElement(calendar);
        SimpleUI.addUIElement(signalMeter);
        SimpleUI.addUIElement(slider);
        SimpleUI.addUIElement(tabs);
        SimpleUI.addUIElement(chat);
        SimpleUI.setUIEventHandler(new UIEventHandler() {
            public void onUIEvent(UIElement element, String action, Object data) {}
        });
    }

    public void draw() {
        SimpleUI.syncHostState();
        SimpleUI.updateAndDrawUI();
    }
}
