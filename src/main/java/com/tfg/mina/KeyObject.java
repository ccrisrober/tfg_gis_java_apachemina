// Copyright (c) 2015, maldicion069 (Cristian Rodríguez) <ccrisrober@gmail.con>
//
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.package com.example

package com.tfg.mina;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class KeyObject implements JSONAware {

    protected int Id;
    protected String color;
    protected float PosX;
    protected float PosY;

    public KeyObject(int id, String color, float posX, float posY) {
        this.Id = id;
        this.color = color;
        this.PosX = posX;
        this.PosY = posY;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getPosX() {
        return PosX;
    }

    public void setPosX(float posX) {
        PosX = posX;
    }

    public float getPosY() {
        return PosY;
    }

    public void setPosY(float posY) {
        PosY = posY;
    }

    public void setPosition(float px, float py) {
        this.PosX = px;
        this.PosY = py;
    }

    public String toJSONString() {
        StringBuffer sb = new StringBuffer();

        sb.append("{");

        sb.append("\"" + JSONObject.escape("Id") + "\"");
        sb.append(":");
        sb.append(Id);
        sb.append(",");

        sb.append("\"" + JSONObject.escape("Color") + "\"");
        sb.append(":");
        sb.append("\"" + JSONObject.escape(color) + "\"");
        sb.append(",");

        sb.append("\"" + JSONObject.escape("PosX") + "\"");
        sb.append(":");
        sb.append(PosX);
        sb.append(",");

        sb.append("\"" + JSONObject.escape("PosY") + "\"");
        sb.append(":");
        sb.append(PosY);

        sb.append("}");

        return sb.toString();
    }
}
