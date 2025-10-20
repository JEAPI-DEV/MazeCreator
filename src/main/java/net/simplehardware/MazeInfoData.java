package net.simplehardware;

import java.util.List;

public class MazeInfoData {

    public String id;
    public String name;
    public List<FormInfo> forms;
    public String maze;
}

class FormInfo {

    public char id;
    public String name;

    public FormInfo() {}

    public FormInfo(char id, String name) {
        this.id = id;
        this.name = name;
    }
}
