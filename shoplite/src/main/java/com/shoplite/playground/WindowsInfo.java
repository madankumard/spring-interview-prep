package com.shoplite.playground;

import com.shoplite.config.OnWindowsCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnWindowsCondition.class)
public class WindowsInfo {

    public WindowsInfo() {
        System.out.println(">>> WindowsInfo created: " + System.getProperty("os.name"));
    }
}
