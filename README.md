# Logisim-evolution+

Logisim-evolution+ is an independent, public fork that continues development
from code originally released under the GNU General Public License version 3
(GPLv3). This repository is maintained separately and is not the official
upstream Logisim-evolution project.

## Short summary

Logisim-evolution+ is a Java-based digital logic design and simulation tool.
This fork focuses on improved canvas interactions (smoother pan and
cursor-anchored zoom), usability refinements, and an explicit, public
development path under GPLv3.

## Licensing and copyright

- This repository contains code derived from GPLv3-licensed work.
- The project and all contributed code in this repository are distributed
	under the terms of the GNU General Public License version 3 (GPLv3).
- Original contributors to the upstream project retain their copyrights
	and their work remains subject to the original GPLv3 terms.

What GPLv3 means in practice:

- You are free to use, study, modify, and distribute the software.
- If you distribute the software (or a derivative work), you must make the
	corresponding source available and license the distributed work under
	GPLv3 as well.
- You must preserve copyright and license notices for the parts you
	distribute, and provide the same rights to downstream recipients.

For the full license text, see the `LICENSE` file included in this
repository.

## Building and running

Requirements:

- Java 21 or newer

Build:

```bash
./gradlew shadowJar
```

Run (after building):

```bash
./logisim-evolution+
```

Show runtime help:

```bash
./logisim-evolution+ --help
```

## Contributing

By contributing to this repository you agree to license your contributions
under GPLv3. Please include clear contributor licensing information in your
pull requests and follow the repository contribution guidelines.

## Disclaimer

This repository is a separate public fork and is not affiliated with the
original Logisim-evolution project. The original Logisim-evolution
contributors retain their copyrights and their code remains under GPLv3.
