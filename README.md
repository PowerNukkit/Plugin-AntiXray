# Anti X-Ray for Nukkit
[![Build](https://img.shields.io/circleci/build/github/wode490390/AntiXray/master)](https://circleci.com/gh/wode490390/AntiXray/tree/master)
[![Release](https://img.shields.io/github/v/release/wode490390/AntiXray)](https://github.com/wode490390/AntiXray/releases)
[![Release date](https://img.shields.io/github/release-date/wode490390/AntiXray)](https://github.com/wode490390/AntiXray/releases)
[![MCBBS](https://img.shields.io/badge/-mcbbs-inactive)](https://www.mcbbs.net/thread-838490-1-1.html "假矿")
<!--[![Servers](https://img.shields.io/bstats/servers/5123)](https://bstats.org/plugin/bukkit/AntiXray/5123)
[![Players](https://img.shields.io/bstats/players/5123)](https://bstats.org/plugin/bukkit/AntiXray/5123)-->

This plugin is used to counter X-RAY Client add-ons.

It modifies data that are sent to clients to hide blocks.

It does not manipulate blocks in the level file, thus is safe to use.

[![](https://i.loli.net/2019/01/27/5c4d21504445e.png)](# "Texture Pack")

[![](https://i.loli.net/2019/08/14/2Wm3haAxELGOB15.png)](# "Toolbox")

If you found any bugs or have any suggestions, please open an issue on [GitHub Issues](https://github.com/wode490390/AntiXray/issues).

If you love this plugin, please star it on [GitHub](https://github.com/wode490390/AntiXray).

## Download
- [Releases](https://github.com/wode490390/AntiXray/releases)
- [Snapshots](https://circleci.com/gh/wode490390/AntiXray)

## Permissions
| Permission | Description | Default |
| - | - | - |
| antixray.whitelist | Allows a player to cheat with X-Ray | OP |

## Configurations

<details>
<summary>config.yml</summary>

```yaml
# The smaller the value, the higher the performance (1~16)
scan-chunk-height-limit: 4
# Save a serialized copy of the chunk in memory for faster sending
memory-cache: true
# Save a serialized copy of the chunk in disk for faster sending
local-cache: true
# Set this to false to use hidden mode
obfuscator-mode: true
# The fake block is used to replace ores in different dimensions (hidden mode only)
overworld-fake-block: 1
nether-fake-block: 87
# Worlds that need to be protected
protect-worlds:
  - "world"
# Blocks that need to be hidden
ores:
  - 14
  - 15
  - 16
  - 21
  - 56
  - 73
  - 74
  - 129
  - 153
# Such as transparent blocks and non-full blocks
filters:
  - 0
  - 6
  - 8
  - 9
  - 10
  - 11
  - 18
  - 20
  - 26
  - 27
  - 28
  - 29
  - 30
  - 31
  - 32
  - 33
  - 34
  - 37
  - 38
  - 39
  - 40
  - 44
  - 50
  - 51
  - 52
  - 53
  - 54
  - 55
  - 59
  - 60
  - 63
  - 64
  - 65
  - 66
  - 67
  - 68
  - 69
  - 70
  - 71
  - 72
  - 75
  - 76
  - 77
  - 78
  - 79
  - 81
  - 83
  - 85
  - 88
  - 90
  - 92
  - 93
  - 94
  - 95
  - 96
  - 101
  - 102
  - 104
  - 105
  - 106
  - 107
  - 108
  - 109
  - 111
  - 113
  - 114
  - 115
  - 116
  - 117
  - 118
  - 119
  - 120
  - 122
  - 126
  - 127
  - 128
  - 130
  - 131
  - 132
  - 134
  - 135
  - 136
  - 138
  - 139
  - 140
  - 141
  - 142
  - 143
  - 144
  - 145
  - 146
  - 147
  - 148
  - 149
  - 150
  - 151
  - 154
  - 156
  - 158
  - 160
  - 161
  - 163
  - 164
  - 165
  - 166
  - 167
  - 171
  - 175
  - 176
  - 177
  - 178
  - 180
  - 182
  - 183
  - 184
  - 185
  - 186
  - 187
  - 190
  - 191
  - 193
  - 194
  - 195
  - 196
  - 197
  - 198
  - 199
  - 200
  - 202
  - 203
  - 204
  - 205
  - 207
  - 208
  - 218
  - 230
  - 238
  - 239
  - 240
  - 241
  - 244
  - 250
  - 253
  - 254
```
</details>

## Compiling
1. Install [Maven](https://maven.apache.org/).
2. Run `mvn clean package`. The compiled JAR can be found in the `target/` directory.

## Metrics Collection

This plugin uses [bStats](https://github.com/wode490390/bStats-Nukkit) - you can opt out using the global bStats config, see the [official website](https://bstats.org/getting-started) for more details.

<!--[![Metrics](https://bstats.org/signatures/bukkit/AntiXray.svg)](https://bstats.org/plugin/bukkit/AntiXray/5123)-->

###### If I have any grammar and terms error, please correct my wrong :)
