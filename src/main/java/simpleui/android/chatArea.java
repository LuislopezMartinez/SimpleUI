package simpleui.android;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.android.SimpleUI.*;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.text.TextWatcher;
import android.text.Editable;
import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.os.Build;

public class chatArea extends UIElement {
    public ArrayList<ChatAreaMessage> items = new ArrayList<ChatAreaMessage>();
    public float internalScrollY = 0;
    public int panelColor = color(255);
    public int leftCardColor = color(244, 245, 247);
    public int rightCardColor = color(226, 239, 255);
    public int borderColor = color(210);
    public int textColor = color(25);
    public float bubbleGap = 8;
    public float bubblePaddingX = 10;
    public float bubblePaddingY = 8;
    public int fontSize = 14;

    public chatArea(String id, int x, int y, int w, int h) {
        super(id, x, y, w, h);
    }

    public void setPanelColor(int nextColor) {
        panelColor = nextColor;
    }

    public void setLeftCardColor(int nextColor) {
        leftCardColor = nextColor;
    }

    public void setRightCardColor(int nextColor) {
        rightCardColor = nextColor;
    }

    public void clearMessages() {
        items.clear();
        internalScrollY = 0;
    }

    public void addMessage(int side, String msg) {
        addMessage(side, msg, "", false);
    }

    public void addMessage(int side, String msg, String footer, boolean readReceipt) {
        ChatAreaMessage item = new ChatAreaMessage();
        item.side = side == RIGHT ? RIGHT : LEFT;
        item.text = msg == null ? "" : msg;
        item.footer = footer == null ? "" : footer;
        item.readReceipt = readReceipt;
        items.add(item);
        internalScrollY = minScroll();
    }

    public float bubbleWidth() {
        return min(width - 40, max(140, width * 0.72f));
    }

    public ArrayList<String> wrappedLines(String value, float maxWidth) {
        ArrayList<String> lines = new ArrayList<String>();
        String safe = value == null ? "" : value;
        String[] paragraphs = split(safe, '\n');
        textSize(fontSize);

        for (int p = 0; p < paragraphs.length; p++) {
            String paragraph = paragraphs[p];
            if (paragraph == null || paragraph.length() == 0) {
                lines.add("");
                continue;
            }

            String[] words = splitTokens(paragraph, " ");
            String currentLine = "";
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (textWidth(candidate) <= maxWidth) {
                    currentLine = candidate;
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine);
                        currentLine = "";
                    }

                    String remaining = word;
                    while (remaining.length() > 0 && textWidth(remaining) > maxWidth) {
                        int splitIndex = remaining.length();
                        while (splitIndex > 1 && textWidth(remaining.substring(0, splitIndex)) > maxWidth) {
                            splitIndex--;
                        }
                        lines.add(remaining.substring(0, splitIndex));
                        remaining = remaining.substring(splitIndex);
                    }
                    currentLine = remaining;
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine);
            }
        }

        if (lines.size() == 0) {
            lines.add("");
        }
        return lines;
    }

    public float bubbleHeight(ChatAreaMessage item) {
        ArrayList<String> lines = wrappedLines(item.text, bubbleWidth() - bubblePaddingX * 2);
        float footerHeight = item.footer.length() > 0 ? 17 : 0;
        return max(42, bubblePaddingY * 2 + lines.size() * (fontSize + 4) + footerHeight);
    }

    public float contentHeight() {
        float total = 8;
        for (int i = 0; i < items.size(); i++) {
            total += bubbleHeight(items.get(i)) + bubbleGap;
        }
        return total;
    }

    public float minScroll() {
        return min(0, height - contentHeight());
    }

    @Override
    public boolean hasScrollableOverflow() {
        return minScroll() < 0;
    }

    public void scrollToBottom() {
        internalScrollY = minScroll();
    }

    public void drawBubble(ChatAreaMessage item, float bubbleX, float bubbleY, float bubbleW, float bubbleH) {
        ArrayList<String> lines = wrappedLines(item.text, bubbleW - bubblePaddingX * 2);
        stroke(item.side == RIGHT ? color(23, 96, 196) : color(190, 195, 200));
        fill(item.side == RIGHT ? rightCardColor : leftCardColor);
        rect(bubbleX, bubbleY, bubbleW, bubbleH, BORDER_RADIUS_MEDIUM);

        fill(textColor);
        textAlign(LEFT, TOP);
        textSize(fontSize);
        float textY = bubbleY + bubblePaddingY;
        for (int i = 0; i < lines.size(); i++) {
            text(lines.get(i), bubbleX + bubblePaddingX, textY);
            textY += fontSize + 4;
        }
        if (item.footer.length() > 0) {
            fill(item.readReceipt ? color(24, 118, 210) : color(112, 120, 132));
            textAlign(RIGHT, BOTTOM);
            textSize(max(10, fontSize - 3));
            text(item.footer, bubbleX + bubbleW - bubblePaddingX,
                bubbleY + bubbleH - bubblePaddingY + 2);
        }
    }

    public void draw() {
        if (!isVisible) return;

        pushStyle();
        fill(panelColor);
        stroke(borderColor);
        strokeWeight(2);
        rect(x, y, width, height, BORDER_RADIUS_MEDIUM);

        if (items.size() == 0) {
            fill(currentTheme.placeholderColor);
            textAlign(CENTER, CENTER);
            textSize(fontSize);
            text("No messages yet.", x + width * 0.5f, y + height * 0.5f);
            popStyle();
            return;
        }

        float bubbleW = bubbleWidth();
        float currentY = y + 8 + internalScrollY;
        for (int i = 0; i < items.size(); i++) {
            ChatAreaMessage item = items.get(i);
            float bubbleH = bubbleHeight(item);
            float bubbleX = item.side == RIGHT ? x + width - bubbleW - 12 : x + 12;
            if (isBubbleFullyVisible(currentY, bubbleH)) {
                drawBubble(item, bubbleX, currentY, bubbleW, bubbleH);
            }
            currentY += bubbleH + bubbleGap;
        }
        popStyle();
    }

    private boolean isBubbleFullyVisible(float bubbleY, float bubbleH) {
        float innerTop = y + 2;
        float innerBottom = y + height - 2;
        return bubbleY >= innerTop && bubbleY + bubbleH <= innerBottom;
    }

    public void mouseDragged() {
        if (!isEnabled) return;

        float mx = getScaledMouseX();
        float my = getScaledMouseY();
        if (!containsPoint(mx, my)) return;

        internalScrollY += (mouseY - pmouseY) / uiScaleY;
        internalScrollY = constrain(internalScrollY, minScroll(), 0);
    }
}
