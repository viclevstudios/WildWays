# Wildways Endermite Short

Der aktuelle Short basiert auf den **zehn echten Wildways-Gameplayaufnahmen** in `../../marketing/endermite-short/` und dem dort abgelegten ElevenLabs-Voiceover. Format: 1080 × 1920, 30 fps, ungefähr 47,9 Sekunden.

- [Gameplay-Schnittplan](VIDEO_PLAN.md)
- [Tatsächlich verwendeter englischer Sprechertext](VOICEOVER_ELEVENLABS.txt)
- [Editierbare Shot- und Untertitelzeiten](gameplay-project/edit.json)
- [Reproduzierbarer Bildschnitt und HyperFrames-Generator](gameplay-project/build_video.py)
- [HyperFrames-Komposition](gameplay-project/index.html)
- Finaler Export auf dem Produktionsrechner: `gameplay-project/renders/wildways-endermite-short.mp4` (wie die Gameplay-Rohclips und das Voiceover nicht im Git-Repository enthalten)

Zum erneuten Bauen auf Windows vom Repository-Stammverzeichnis aus `python social/endermite-short/gameplay-project/build_video.py` ausführen. Danach im Ordner `social/endermite-short/gameplay-project` mit `../node_modules/.bin/hyperframes.cmd check` prüfen und mit `../node_modules/.bin/hyperframes.cmd render --quality delivery --output renders/wildways-endermite-short.mp4` rendern. Lokale FFmpeg- und FFprobe-Binärverzeichnisse aus `node_modules` müssen dabei im `PATH` liegen.

Der ältere Ordner `project/` ist ein **verworfenes Motion-Graphics-Experiment**. Sein MP4 und sein Skript sind keine aktuelle Fassung. Das aktuelle Video verwendet keine KI-generierten Bilder oder Bildteile. Die Stimme ist KI-generiertes ElevenLabs-Audio.
