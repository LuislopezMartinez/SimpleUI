# Third-party audio components

SimpleUI embeds the following Java 8 Soundlibs components so Processing
Desktop can decode MP3 and Ogg Vorbis without a separate installation:

- MP3SPI 1.9.5.4
- VorbisSPI 1.0.3.3
- JLayer 1.0.1.4
- JOrbis 0.0.17.4
- Tritonus Share 0.3.7.4

The artifacts are distributed by the Soundlibs project under the GNU Lesser
General Public License version 2.1. Their unmodified JAR files are kept in
`deps/audio`, and those original JARs are embedded as private Desktop runtime
resources in the distributed SimpleUI JAR. They are extracted to a temporary
cache only when Desktop audio is first used; Android does not load them.
SimpleUI's own source code remains licensed under MIT.

Project and source: https://github.com/pdudits/soundlibs

License text: `licenses/LGPL-2.1.txt`
