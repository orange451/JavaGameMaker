package net.mantagames.jgm.engine.skeleton;

import net.mantagames.jgm.engine.GameObject;

public interface GameObjectInterface {
	public void event_create();
	public void event_begin_step();
	public void event_step();
	public void event_end_step();
	public void event_draw();
	public void event_destroy();
	public void event_animation_end();
	public void event_collide(GameObject obj);
	public void event_outside_room();
	public void event_out_of_lives();
	public void event_out_of_health();
	public void event_room_start();
	public void event_room_end();
	public void event_game_start();
	public void event_game_end();
	
	public void event_user_0();
	public void event_user_1();
	public void event_user_2();
	public void event_user_3();
	public void event_user_4();
	public void event_user_5();
	public void event_user_6();
	public void event_user_7();
	public void event_user_8();
	public void event_user_9();
	public void event_user_10();
	public void event_user_11();
	
	public void event_alarm_0();
	public void event_alarm_1();
	public void event_alarm_2();
	public void event_alarm_3();
	public void event_alarm_4();
	public void event_alarm_5();
	public void event_alarm_6();
	public void event_alarm_7();
	public void event_alarm_8();
	public void event_alarm_9();
	public void event_alarm_10();
	public void event_alarm_11();
}
