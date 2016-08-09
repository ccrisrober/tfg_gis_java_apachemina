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

import java.util.HashMap;
import java.util.Map;

public class RealObjects {

    private static final RealObjects INSTANCE = new RealObjects();

    protected Map<String, KeyObject> objects = new HashMap<String, KeyObject>();

    private RealObjects() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static RealObjects getInstance() {
        return INSTANCE;
    }

    public KeyObject getObject(String id) {
        return objects.get(id);
    }

    public void setPositionObject(String id, float posX, float posY) {
        KeyObject o = getObject(id);
        o.setPosition(posX, posY);
        this.addObject(id, o);
    }

    public void addObject(String id, KeyObject o) {
        this.objects.put(id, o);
    }
}
