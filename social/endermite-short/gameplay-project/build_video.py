"""Build the portrait Wildways gameplay edit and HyperFrames composition.

Run from the repository root with Python 3. The source clips stay untouched.
"""

from __future__ import annotations

import html
import json
import shutil
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).resolve().parent
MEDIA = ROOT / "marketing" / "endermite-short"
TOOLS = ROOT / "social" / "endermite-short" / "node_modules"
FFMPEG = TOOLS / "@ffmpeg-installer" / "win32-x64" / "ffmpeg.exe"
FFPROBE = TOOLS / "@ffprobe-installer" / "win32-x64" / "ffprobe.exe"
ASSETS = HERE / "assets"
ASSETS.mkdir(exist_ok=True)

VOICE = next(MEDIA.glob("ElevenLabs*.mp3"))
VOICE_DURATION = float(
    subprocess.check_output(
        [str(FFPROBE), "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", str(VOICE)],
        text=True,
    ).strip()
)

# Every source range plays at its original speed. Cuts remove idle camera travel
# and pauses while the final shot ends exactly with the narration.
SHOTS = [
    ("Endermite Intro.mp4", 0.0, 3.69, "center"),
    ("Endermite Walking.mp4", 0.0, 4.12, "follow"),
    ("Endermite Shell Drop.mp4", 3.5, 7.13, "center"),
    ("Endermite Bricks House Building.mp4", 1.5, 6.8, "portrait"),
    ("Endermite Nest.mp4", 5.0, 7.16, "center"),
    ("Endermite Nest.mp4", 9.4, 10.7, "center"),
    ("Endermite Nest.mp4", 11.4, 13.3, "center"),
    ("Endermite Nest.mp4", 14.1, 15.2, "center"),
    ("Endermite Nest.mp4", 17.19, 19.51, "center"),
    ("Endermite Nest.mp4", 19.51, 21.16, "center"),
    ("Eye of Ender to Eye of the Endermites.mp4", 0.0, 3.72, "center"),
    ("Potion of Unease.mp4", 2.0, 3.7, "center"),
    ("Potion of Unease.mp4", 5.2, 6.3, "center"),
    ("Potion of Unease.mp4", 9.3, 11.75, "center"),
    ("Potion of Unease.mp4", 13.6, 15.83, "center"),
    ("Quarantine Grounds.mp4", 3.2, 7.73, "portrait"),
    ("Using Eye of the Endermites.mp4", 2.3, 3.5, "portrait"),
    ("Using Eye of the Endermites.mp4", 6.1, 8.2, "portrait"),
    ("Endermite Outro.mp4", 1.0, 1.0 + VOICE_DURATION - 46.2, "portrait"),
]

# These are speech windows based on the dry ElevenLabs take's actual pauses.
# Each entry is verbatim from the user's updated voiceover file.
CAPTIONS = [
    (0.00, 2.34, "What if Minecraft's most forgotten mob"),
    (2.50, 3.43, "finally mattered?"),
    (3.99, 6.11, "Wildways completely improves the Endermite"),
    (6.25, 8.25, "and adds some special items along the way."),
    (8.72, 11.03, "Defeat one for a chance at an Endermite Shell."),
    (11.43, 14.03, "A shell and stone bricks make Endermite Bricks,"),
    (14.10, 16.38, "with matching stairs, slabs, and walls."),
    (16.74, 19.75, "Eight shells and a chest make a portable Endermite Nest."),
    (20.05, 22.44, "Its twelve slots keep their contents when you move it."),
    (22.87, 25.09, "Opening it might release another endermite though."),
    (25.52, 26.98, "Leave an Eye of Ender inside."),
    (27.32, 30.42, "After some time, it transforms into the Eye of the Endermite."),
    (30.89, 33.09, "Brew a shell into a potion of Unease."),
    (33.41, 35.72, "Break solid blocks, and Endermites may appear."),
    (36.10, 38.13, "Level 2 Unease doubles the chance."),
    (38.37, 41.22, "And you'll find camps infested by endermites around the world."),
    (41.58, 42.97, "Watch out for hidden secrets there."),
    (43.33, 46.02, "These features and lots more is part of WildWays."),
    (46.22, 47.79, "Releasing on November 1st."),
]

LABELS = [
    (0.05, 3.7, "THE FORGOTTEN MOB"),
    (4.0, 8.5, "NO FIXED DESPAWN TIMER"),
    (8.72, 11.35, "ENDERMITE SHELL"),
    (11.45, 16.5, "BRICKS · STAIRS · SLABS · WALLS"),
    (16.75, 19.9, "8 SHELLS + CHEST"),
    (20.0, 21.35, "12 SLOTS · PORTABLE"),
    (21.35, 22.7, "WATERLOGGABLE · COMPARATOR"),
    (22.88, 25.2, "5% CHANCE ON OPEN"),
    (25.55, 27.2, "EYE OF ENDER + NEST"),
    (27.3, 30.6, "RANDOM WAIT · ~10 MIN AVG"),
    (30.9, 33.1, "POTION OF UNEASE"),
    (33.4, 35.8, "UNEASE · 10% PER BLOCK"),
    (36.1, 37.1, "UNEASE II · 20% PER BLOCK"),
    (37.1, 38.2, "CREEPER BLASTS ALSO COUNT"),
    (38.4, 42.95, "QUARANTINE GROUNDS"),
    (43.32, 46.1, "EYE: 1 OF 12 UNIQUE EYES"),
    (46.2, VOICE_DURATION, "WILDWAYS · NOVEMBER 1"),
]


def make_montage() -> None:
    inputs = list(dict.fromkeys(s[0] for s in SHOTS))
    assert abs(sum(end - begin for _, begin, end, _ in SHOTS) - VOICE_DURATION) < 0.000001
    args = [str(FFMPEG), "-y", "-hide_banner", "-loglevel", "warning"]
    for name in inputs:
        args += ["-i", str(MEDIA / name)]
    filters = []
    for i, (name, begin, end, framing) in enumerate(SHOTS):
        index = inputs.index(name)
        length = end - begin
        if framing == "portrait":
            treatment = "scale=1080:1920:flags=lanczos"
        else:
            if framing == "follow":
                # Track the mite traveling left to right, keeping it in frame.
                x = "580+74*t"
            else:
                x = "656"
            treatment = f"crop=608:1080:{x}:0,scale=1080:1920:flags=lanczos"
        filters += [
            f"[{index}:v]trim=start={begin}:end={end},setpts=PTS-STARTPTS,"
            f"fps=30,trim=duration={length:.9f},setpts=PTS-STARTPTS,"
            f"{treatment},setsar=1,format=yuv420p[v{i}]",
            f"[{index}:a]atrim=start={begin}:end={end},asetpts=PTS-STARTPTS,"
            f"aresample=48000,atrim=duration={length:.9f}[a{i}]",
        ]
    joined = "".join(f"[v{i}][a{i}]" for i in range(len(SHOTS)))
    filters.append(f"{joined}concat=n={len(SHOTS)}:v=1:a=1[v][a]")
    target = ASSETS / "gameplay-base.mp4"
    args += [
        "-filter_complex", ";".join(filters), "-map", "[v]", "-map", "[a]",
        "-c:v", "libx264", "-preset", "medium", "-crf", "18", "-g", "30", "-keyint_min", "30", "-pix_fmt", "yuv420p",
        "-c:a", "aac", "-b:a", "192k", "-movflags", "+faststart", str(target),
    ]
    print(f"Building {target} from {len(SHOTS)} gameplay shots", flush=True)
    subprocess.run(args, check=True)


def make_composition() -> None:
    shutil.copy2(VOICE, ASSETS / "voiceover.mp3")
    caption_elements = []
    for i, (start, end, words) in enumerate(CAPTIONS):
        caption_elements.append(
            f'<div class="clip caption" data-start="{start:.3f}" data-duration="{end-start:.3f}" '
            f'data-track-index="2" id="caption-{i}"><span>{html.escape(words)}</span></div>'
        )
    label_elements = []
    for i, (start, end, words) in enumerate(LABELS):
        label_elements.append(
            f'<div class="clip label" data-start="{start:.3f}" data-duration="{end-start:.3f}" '
            f'data-track-index="3" id="label-{i}"><span>{html.escape(words)}</span></div>'
        )
    document = f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=1080, height=1920">
<title>Wildways Endermite Update — Gameplay Short</title>
<style>
  * {{box-sizing:border-box}}
  html,body {{width:1080px;height:1920px;margin:0;overflow:hidden;background:#07100c}}
  body {{font-family:Arial,Helvetica,sans-serif;color:white}}
  #root {{position:relative;width:1080px;height:1920px;overflow:hidden;background:#07100c}}
  .clip {{position:absolute}}
  .gameplay {{inset:0;width:1080px;height:1920px;object-fit:cover;z-index:1}}
  .shade {{position:absolute;inset:0;z-index:2;pointer-events:none;
    background:linear-gradient(to bottom,rgba(0,0,0,.56),transparent 18%,transparent 57%,rgba(0,0,0,.25) 72%,rgba(0,0,0,.65))}}
  .brand {{position:absolute;left:70px;top:70px;z-index:5;padding:13px 19px;
    font-size:27px;font-weight:900;letter-spacing:.15em;color:#f8ffe7;
    background:rgba(11,21,16,.73);border-left:7px solid #b8d693;text-shadow:0 3px 6px #000}}
  .label {{z-index:5;top:195px;left:70px;right:86px;font-size:38px;font-weight:950;
    letter-spacing:.06em;line-height:1.1;text-transform:uppercase;text-shadow:0 4px 12px #000,0 0 25px #000}}
  .label span {{display:inline-block;padding:13px 19px;background:rgba(9,18,14,.76);
    border-left:7px solid #b8d693;max-width:100%}}
  .caption {{z-index:6;left:66px;right:66px;bottom:262px;min-height:150px;
    display:flex;align-items:center;justify-content:center;text-align:center;
    font-size:57px;font-weight:950;letter-spacing:-.025em;line-height:1.12;
    text-shadow:0 5px 5px #000,0 0 18px #000,0 0 34px #000}}
  .caption span {{display:inline-block;max-width:950px;text-wrap:balance}}
  .voice {{display:none}}
</style>
</head>
<body>
<div id="root" data-composition-id="main" data-start="0" data-duration="{VOICE_DURATION:.6f}" data-width="1080" data-height="1920" data-fps="30" data-no-timeline>
  <video id="gameplay" class="clip gameplay" data-start="0" data-duration="{VOICE_DURATION:.6f}" data-track-index="0" data-has-audio="true" data-volume="0.09" src="assets/gameplay-base.mp4" playsinline></video>
  <audio id="voiceover" class="clip voice" data-start="0" data-duration="{VOICE_DURATION:.6f}" data-track-index="1" data-volume="1" src="assets/voiceover.mp3"></audio>
  <div class="shade"></div>
  <div class="brand">WILDWAYS</div>
  {''.join(label_elements)}
  {''.join(caption_elements)}
</div>
</body>
</html>'''
    (HERE / "index.html").write_text(document, encoding="utf-8")
    (HERE / "edit.json").write_text(
        json.dumps({"voice_seconds": VOICE_DURATION, "shots": SHOTS, "captions": CAPTIONS, "labels": LABELS}, indent=2),
        encoding="utf-8",
    )


if __name__ == "__main__":
    make_montage()
    make_composition()
