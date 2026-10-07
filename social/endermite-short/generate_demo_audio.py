"""Synthesize a quiet, original placeholder bed for the silent voiceover test."""

from array import array
from math import pi, sin
from pathlib import Path
import wave

RATE = 22_050
DURATION = 60
OUT = Path(__file__).parent / "project" / "assets" / "demo-bed.wav"
OUT.parent.mkdir(parents=True, exist_ok=True)

samples = array("h")
cuts = (0, 7, 13, 19, 31, 40, 51, 56)
for i in range(RATE * DURATION):
    t = i / RATE
    fade = min(1.0, t / 1.3, (DURATION - t) / 1.5)
    pulse = 0.55 + 0.45 * sin(2 * pi * 0.42 * t) ** 2
    drone = 0.10 * sin(2 * pi * 110 * t) + 0.035 * sin(2 * pi * 164.81 * t)
    chime = sum(
        0.10 * max(0.0, 1.0 - (t - start) / 0.75)
        * sin(2 * pi * (440 if n % 2 else 329.63) * (t - start))
        for n, start in enumerate(cuts)
        if 0 <= t - start < 0.75
    )
    samples.append(int(max(-1.0, min(1.0, (drone * pulse + chime) * fade)) * 32767))

with wave.open(str(OUT), "wb") as wav:
    wav.setnchannels(1)
    wav.setsampwidth(2)
    wav.setframerate(RATE)
    wav.writeframes(samples.tobytes())

print(OUT)
