# Pin-Number-Picker
A librarifcation of the PinNumberPicker from the AndroidTV Settings app, with a couple additional goodies.

![screenshot](https://raw.githubusercontent.com/firekesti/Pin-Number-Picker/master/screenshot.png)

## How to Use  
The sample app has a working example, but here's the gist of it: one PinNumberPicker is one vertical carousel of numbers. So, align however many of them you need in your layout, then connect them in Java:  
```xml
<net.firekesti.pinnumberpicker.PinNumberPicker
        android:id="@+id/picker1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
<net.firekesti.pinnumberpicker.PinNumberPicker
        android:id="@+id/picker2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
```

```java
PinNumberPicker numberPicker1 = (PinNumberPicker) findViewById(R.id.picker1);
PinNumberPicker numberPicker2 = (PinNumberPicker) findViewById(R.id.picker2);
numberPicker1.setValueRange(0, 9);
numberPicker1.setNextNumberPicker(numberPicker2);
```

By using `setNextNumberPicker`, you allow the user to go from one `PinNumberPicker` to the next by using the Enter or D-pad Center button. It also ties in with the `OnFinalNumberDoneListener`, which will be called whenever there is no "next" `PinNumberPicker` in the sequence. This allows you to read the number values once the user presses Enter/Center on the last picker.  

## Arrows  
The Android TV settings app version does not have arrows. I decided to add them as an optional feature! Enabling them is a two-step process. First:  
```java
numberPicker1.setArrowsEnabled(true);
numberPicker2.setArrowsEnabled(true);
```
Then, in your `dimens.xml`:  
```xml
<dimen name="pin_number_picker_height">@dimen/pin_number_picker_height_with_arrows</dimen>
```
This is covered in the sample app as well.

## How to Style  
You can change the color of the number text, the focus-background color, and the arrow color like this:  
```xml
<color name="pin_number_picker_focused_background">@color/colorPrimaryDark</color>
<color name="pin_picker_arrow_color">@color/colorAccent</color>
<color name="pin_picker_text_color">@color/colorTextLight</color>
```

You can change the opacity of the numbers, when focused and not, like this:  
```xml
<item name="pin_alpha_for_focused_number" type="dimen" format="float">0.5</item>
<item name="pin_alpha_for_adjacent_number" type="dimen" format="float">0.1</item>
```

You can slow down or speed up the animations like this:  
```xml
<integer name="pin_number_scroll_duration">500</integer>
<integer name="pin_number_scroll_anim_duration">250</integer>
```

Finally, you can change the size this way:  
```xml
<dimen name="pin_number_picker_text_size">32sp</dimen>
<dimen name="pin_number_picker_text_view_width">64dp</dimen>
<dimen name="pin_number_picker_text_view_height">64dp</dimen>
<dimen name="pin_number_picker_width">64dp</dimen>
<dimen name="pin_number_picker_height_with_arrows">320dp</dimen>
<dimen name="pin_number_picker_height_standard">192dp</dimen>
```
### Please make sure!!!  
###`pin_number_picker_height_standard` should be 3x `pin_number_picker_text_view_height`  
###`pin_number_picker_height_with_arrows` should be 5x `pin_number_picker_text_view_height`

Okay, I think that's it! Please file an Issue if you have any questions or see any bugs!
