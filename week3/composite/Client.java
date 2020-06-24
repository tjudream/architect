package com.tjudream.designpattern.composite.arch;

public class Client {
    public static void main(String[] args) {
        Composite root = new Composite("WinForm(WINDOW窗口)");
        Leaf picture = new Leaf("Picture(LOGO图片)");
        Leaf buttonLogin = new Leaf("Button(登录)");
        Leaf buttonReg = new Leaf("Buttor(注册)");
        Composite frame = new Composite("Frame(FRAME1)");
        Leaf lableName = new Leaf("Lable(用户名)");
        Leaf textBox = new Leaf("TextBox(文本框)");
        Leaf lablePass = new Leaf("Lable(密码)");
        Leaf passwordBox = new Leaf("PasswordBox(密码框)");
        Leaf checkBox = new Leaf("CheckBox(复选框)");
        Leaf textBoxName = new Leaf("TextBox(记住用户名)");
        Leaf linkLable = new Leaf("LinkLable(忘记密码)");
        root.add(picture);
        root.add(buttonLogin);
        root.add(buttonReg);
        root.add(frame);
        frame.add(lableName);
        frame.add(textBox);
        frame.add(lablePass);
        frame.add(passwordBox);
        frame.add(checkBox);
        frame.add(textBoxName);
        frame.add(linkLable);
        root.display();
    }
}
