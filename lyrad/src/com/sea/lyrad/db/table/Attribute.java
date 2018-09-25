package com.sea.lyrad.db.table;

import com.sea.lyrad.exec.DBProcessException;

public class Attribute {
    private String name;
    private DataType type;

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    private int length;

    public Attribute(String name, String type, String length) {
        this.name = name;
        for (DataType dataType : DataType.values()) {
            if (dataType.name().equals(type)) {
                this.type = dataType;
            }
            break;
        }
        if (length == null) {
            this.length = -1;
            return;
        }
        this.length = Integer.parseInt(length);
    }

    public Attribute(String name, String type, int length) {
        this.name = name;
        for (DataType dataType : DataType.values()) {
            if (dataType.name().equals(type)) {
                this.type = dataType;
            }
            break;
        }
        this.length = length;
    }

    public void checkType(String value) throws DBProcessException {
        if (type == DataType.VARCHAR) {
            if (value.length() > length) {
                throw new DBProcessException(String.format("The length of '%s' is outsize.", value));
            }
        } else if (type == DataType.INT) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new DBProcessException(String.format("The format of value '%s' is not right.", value));
            }
        }
    }
}
