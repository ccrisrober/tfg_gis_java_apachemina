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

import java.util.Set;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

// TODO (Sin hacer en ningún sitio): Si muere un usuario o se pira, los objetos no se devuelven al mapa D:
public class ObjectUser implements JSONAware {

    protected long Id;
    protected float PosX;
    protected float PosY;
    protected int Map;
    protected int RollDice;
    protected java.util.Set<Integer> Objects;

    public ObjectUser(long id, float posX, float posY, int map, int rollDice,
            Set<Integer> objects) {
        Id = id;
        PosX = posX;
        PosY = posY;
        Map = map;
        RollDice = rollDice;
        Objects = objects;
    }

    public ObjectUser(long id, float posX, float posY) {
        this(id, posX, posY, 0, 0, new java.util.concurrent.ConcurrentSkipListSet<Integer>());
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
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

    public int getMap() {
        return Map;
    }

    public void setMap(int map) {
        Map = map;
    }

    public int getRollDice() {
        return RollDice;
    }

    public void setRollDice(int rollDice) {
        RollDice = rollDice;
    }

    public void addObj(int idx) {
        this.Objects.add(idx);
    }

    public void remObj(int idx) {
        this.Objects.remove(idx);
    }

    public String toJSONString() {
        StringBuffer sb = new StringBuffer();

        sb.append("{");

        sb.append("\"" + JSONObject.escape("Id") + "\"");
        sb.append(":");
        sb.append(Id);
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

    @Override
    public String toString() {
        return "ObjectUser [Id=" + Id + ", PosX=" + PosX + ", PosY=" + PosY
                + ", Map=" + Map + ", RollDice=" + RollDice + ", Objects="
                + Objects + "]";
    }

}
