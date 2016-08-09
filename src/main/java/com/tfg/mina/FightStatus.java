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

import java.util.concurrent.atomic.AtomicInteger;

public class FightStatus {

    public enum Status {

        None, Init, NeedACK, OKInit, Fight, FinalFight
    };

    private int id_battle;
    private long id_emisor;
    private long id_receiver;
    private Status state;
    private AtomicInteger semaphore;
    private boolean finish;

    public FightStatus(int id_battle, long id_emisor, long id_receiver) {
        this.id_battle = id_battle;
        this.id_emisor = id_emisor;
        this.id_receiver = id_receiver;
        this.state = Status.None;
        this.finish = false;
        this.semaphore = new AtomicInteger(0);
    }

    public Status getState() {
        return state;
    }

    public void addPlayer() {
        this.semaphore.incrementAndGet();
    }

    public int getPlayers() {
        return this.semaphore.get();
    }

    public void setState(Status state) {
        if (state == Status.FinalFight) {
            this.finish = true;
        }
        this.state = state;
    }

    public int getId_battle() {
        return id_battle;
    }

    public long getId_emisor() {
        return id_emisor;
    }

    public long getId_receiver() {
        return id_receiver;
    }

    public boolean isFinish() {
        return finish;
    }

}
