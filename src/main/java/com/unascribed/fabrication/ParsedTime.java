package com.unascribed.fabrication;


import java.math.BigDecimal;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;

public class ParsedTime {

	public static final ParsedTime UNSET = new ParsedTime(6000, false);
	public static final ParsedTime FOREVER = new ParsedTime(Integer.MAX_VALUE, false);
	public static final ParsedTime INVINCIBLE = new ParsedTime(Integer.MAX_VALUE, false);
	public static final ParsedTime INSTANTLY = new ParsedTime(0, false);
	
	public final int timeInTicks;
	public final boolean priority;
	
	private ParsedTime(int timeInTicks, boolean priority) {
		super();
		this.timeInTicks = timeInTicks;
		this.priority = priority;
	}
	
	public boolean overshadows(ParsedTime that) {
		if (that.priority && !this.priority) return false;
		if (this.priority && !that.priority) return true;
		return this.timeInTicks > that.timeInTicks;
	}
	
	@Override
	public String toString() {
		if (this == UNSET) return "ParsedTime.UNSET";
		if (this == FOREVER) return "ParsedTime.FOREVER";
		if (this == INVINCIBLE) return "ParsedTime.INVINCIBLE";
		if (this == INSTANTLY) return "ParsedTime.INSTANTLY";
		return "ParsedTime{"+timeInTicks+"t"+(priority?"!":"")+"}";
	}
	
	public static ParsedTime getFrom(QDIni cfg, String k) {
		String v = cfg.get(k).orElse("");
		try {
			return parse(v);
		} catch (IllegalArgumentException e) {
			FabLog.warn(k+" must be one of unset, forever, f, invincible, invulnerable, i, instantly, or a timespec like 30s (got "+v+") at "+cfg.getBlame(k));
			return UNSET;
		}
	}
	
	public static ParsedTime parse(String time) {
		Preconditions.checkNotNull(time);
		boolean priority = false;
		if (time.endsWith("!")) {
			priority = true;
			time = time.substring(0, time.length()-1);
		}
		switch (time) {
			case "unset":
				return UNSET;
			case "forever": case "f":
				return FOREVER;
			case "invincible": case "invulnerable": case "i":
				return INVINCIBLE;
			case "instantly": case "0":
				return INSTANTLY;
		}
		int multiplier;
		char qualifier = time.charAt(time.length()-1);
		String timeNumPart = time.substring(0, time.length()-1);
		if (!CharMatcher.digit().matchesAllOf(timeNumPart)) {
			throw new IllegalArgumentException("Bad timespec "+time);
		}
		switch (qualifier) {
			case 't': multiplier = 1; break;
			case 's': multiplier = 20; break;
			case 'm': multiplier = 20*60; break;
			case 'h': multiplier = 20*60*60; break;
			default: throw new IllegalArgumentException("Unknown qualifier "+qualifier+" for time value "+time);
		}
		return new ParsedTime(new BigDecimal(timeNumPart).multiply(new BigDecimal(multiplier)).intValueExact(), priority);
	}
	
}
