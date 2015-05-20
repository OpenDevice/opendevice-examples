/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.android.btkeyboard;


import android.view.KeyEvent;

import java.util.LinkedList;
import java.util.List;

import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 26/10/14.
 */
public class SnessController extends Sensor {

    private List<Button> lastPressed;

    public void setLastPressed(List<Button> lastPressed) {
        this.lastPressed = lastPressed;
    }

    public List<Button> getLastPressed() {
        return lastPressed;
    }

    public static interface Button{

        int getButtonIndex();
        int getButtonCode();

    }

    public static enum Pad1 implements Button{


        B(0, KeyEvent.KEYCODE_BUTTON_B),
        Y(1, KeyEvent.KEYCODE_BUTTON_Y),
        SELECT(2, KeyEvent.KEYCODE_BUTTON_SELECT),
        START(3, KeyEvent.KEYCODE_BUTTON_START),
        UP(4, KeyEvent.KEYCODE_DPAD_UP),
        DOWN(5, KeyEvent.KEYCODE_DPAD_DOWN),
        LEFT(6, KeyEvent.KEYCODE_DPAD_LEFT),
        RIGHT(7, KeyEvent.KEYCODE_DPAD_RIGHT),
        A(8, KeyEvent.KEYCODE_BUTTON_A),
        X(9, KeyEvent.KEYCODE_BUTTON_X),
        L(10, KeyEvent.KEYCODE_BUTTON_L1),
        R(11, KeyEvent.KEYCODE_BUTTON_R1),

//        B(0, KeyEvent.KEYCODE_B),
//        Y(1, KeyEvent.KEYCODE_Y),
//        SELECT(2, KeyEvent.KEYCODE_BUTTON_SELECT),
//        START(3, KeyEvent.KEYCODE_BUTTON_START),
//        UP(4, KeyEvent.KEYCODE_DPAD_UP),
//        DOWN(5, KeyEvent.KEYCODE_DPAD_DOWN),
//        LEFT(6, KeyEvent.KEYCODE_DPAD_LEFT),
//        RIGHT(7, KeyEvent.KEYCODE_DPAD_RIGHT),
//        A(8, KeyEvent.KEYCODE_A),
//        X(9, KeyEvent.KEYCODE_X),
//        L(10, KeyEvent.KEYCODE_L),
//        R(11, KeyEvent.KEYCODE_R),


//        B(0, 'B'),
//        Y(1, 'Y'),
//        SELECT(2, 'Q'),
//        START(3, 'W'),
//        UP(4, '1'),
//        DOWN(5, '2'),
//        LEFT(6, '3'),
//        RIGHT(7, '4'),
//        A(8, 'A'),
//        X(9, 'X'),
//        L(10,'L'),
//        R(11, 'R'),
        ;

        Pad1(int index, int code) {
            this.index = index;
            this.code = code;
        }

        int index;
        int code;

        @Override
        public int getButtonIndex() {
            return index;
        }

        @Override
        public int getButtonCode() {
            return code;
        }
    }

    public SnessController(int uid) {
        super(uid, DeviceType.CHARACTER);
    }

    public boolean isPressed(Button button){
        return checkByte(button.getButtonIndex());
    }

    public Button getPressedPad1(){

        List<Button> buttons = getPressed(Pad1.values());

        if(buttons.size() > 0) return buttons.get(0);

        return null;

    }

    public List<Button> getPressed(Button[] buttons){
        List<Button> pressed = new LinkedList<Button>();
        for (Button button : buttons) {
            if (isPressed(button)) {
                pressed.add(button);
            }
        }

        return pressed;
    }

    private boolean checkByte(int byteIndex){
        return (( getValue() >>byteIndex) & 1) != 0;
    }


    public static void main(String[] args) {



        System.out.println(Integer.toBinaryString(~1));

        SnessController controller = new SnessController(1);
        controller.setValue(0x00000000000000000000000000000001);

        Pad1[] values = Pad1.values();
        for (Pad1 value : values) {
            System.out.println(value + " - " + controller.isPressed(value));
        }


    }

}
