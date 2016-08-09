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

public class Map implements JSONAware {

    protected int Id;
    protected String MapFields;
    protected int Width;
    protected int Height;
    protected java.util.Map<String, KeyObject> Objects;

    public Map(int id, String mapFields, int width, int height,
            KeyObject... objects) {
        this.Id = id;
        this.MapFields = mapFields;
        this.Width = width;
        this.Height = height;
        this.Objects = new java.util.concurrent.ConcurrentHashMap<String, KeyObject>();
        for (KeyObject ko : objects) {
            this.Objects.put(ko.getId() + "", ko);
            RealObjects.getInstance().addObject(ko.getId() + "", ko);
        }
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getMapFields() {
        return MapFields;
    }

    public void setMapFields(String mapFields) {
        MapFields = mapFields;
    }

    public int getWidth() {
        return Width;
    }

    public void setWidth(int width) {
        Width = width;
    }

    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
    }

    public java.util.Map<String, KeyObject> getObjects() {
        return Objects;
    }

    public void setObjects(java.util.Map<String, KeyObject> objects) {
        Objects = objects;
    }

    public synchronized KeyObject addObject(int id_obj, float x, float y) {
        RealObjects.getInstance().setPositionObject(id_obj + "", x, y);
        KeyObject ko = RealObjects.getInstance().getObject(id_obj + "");
        this.Objects.put(id_obj + "", ko);
        return ko;
    }

    public synchronized KeyObject removeObject(int id_obj) {
        return this.Objects.remove(id_obj);
    }

    public String toJSONString() {
        StringBuffer sb = new StringBuffer();

        sb.append("{");

        sb.append("\"" + JSONObject.escape("Id") + "\"");
        sb.append(":");
        sb.append(Id);
        sb.append(",");

        sb.append("\"" + JSONObject.escape("MapFields") + "\"");
        sb.append(":");
        sb.append("\"" + JSONObject.escape(MapFields) + "\"");
        sb.append(",");

        sb.append("\"" + JSONObject.escape("Width") + "\"");
        sb.append(":");
        sb.append(Width);
        sb.append(",");

        sb.append("\"" + JSONObject.escape("Height") + "\"");
        sb.append(":");
        sb.append(Height);
        sb.append(",");

        sb.append("\"" + JSONObject.escape("KeyObjects") + "\"");
        sb.append(":");
        sb.append(JSONObject.toJSONString(Objects));

        sb.append("}");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Map [Id=" + Id + ", MapFields=" + MapFields + ", Width="
                + Width + ", Height=" + Height + ", Objects=" + Objects + "]";
    }

}
