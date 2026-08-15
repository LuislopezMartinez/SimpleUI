import processing.core.PApplet;
import simpleui.android.SimpleUI;
import simpleui.android.UIDropdown;
import simpleui.android.UIElement;
import simpleui.android.UIEventHandler;

public class AndroidDropdownRegression {
    public static void main(String[] args) {
        PApplet host = new PApplet();
        host.width = 320;
        host.height = 240;

        SimpleUI.setAutomaticEventHandling(false);
        SimpleUI.attach(host);

        final int[] changes = {0};
        UIDropdown dropdown = new UIDropdown(
            "mode", 10, 60, 100, 30, new String[]{"A", "B", "C"}, 14
        );
        SimpleUI.addUIElement(dropdown);
        SimpleUI.setUIEventHandler(new UIEventHandler() {
            public void onUIEvent(UIElement element, String action, Object data) {
                if (element == dropdown && "changed".equals(action)) {
                    changes[0]++;
                }
            }
        });

        dropdown.setOpen(true);
        SimpleUI.activeDropdown = dropdown;
        SimpleUI.gestureState.pressedElement = dropdown;

        // The second expanded row is outside the dropdown's closed bounds.
        // This is the Android path that regressed before SimpleUI 0.5.3.
        SimpleUI.performTapAction(20, 135, 135);

        if (dropdown.getSelectedIndex() != 1 ||
            !"B".equals(dropdown.getSelectedValue()) ||
            changes[0] != 1 ||
            dropdown.isOpen()) {
            throw new AssertionError(
                "Expanded Android dropdown rows must apply the selected value"
            );
        }

        SimpleUI.detach();
        System.out.println("Android dropdown selection regression passed.");
    }
}
