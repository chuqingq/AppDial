import re
import json

reShortcutInfo = re.compile(r' {10}ShortcutInfo [^\n]*(\n {12}(?P<name>[^\n=]+)=(?P<value>[^\n]+))+', re.DOTALL)

rePackageName = re.compile(r'packageName=(.*)')
reShortLabel = re.compile(r'shortLabel=(.*),')
reIntents = re.compile(r'intents=.*', re.M)
reIntentsAct = re.compile(r'act=([^ ]+)')
reIntentsDat = re.compile(r'dat=([^ ]+)')
reIntentsCat = re.compile(r'cat=\[(.*)\]')
reIntentsFlg = re.compile(r'flg=([^ ]+)')
reIntentsCmp = re.compile(r'cmp=([^/ ]+)/([^/ ]+).*', re.M)
reIntentsPb = re.compile(r'PersistableBundle\[\{(.*)\}\]', re.M)
reIntentsPbAttr = re.compile(r'([^ =]+)=([^ ,]+)')

shortcuts = []

with open('dumpsys-shortcut.txt') as f:
    content = f.read()
    for shortcutInfo in reShortcutInfo.finditer(content, re.M):
        shortcut = {}
        s = shortcutInfo.group()
        # packageName
        shortcut['packageName'] = rePackageName.search(s).group(1)
        # shortLabel
        shortcut['shortLabel'] = reShortLabel.search(s).group(1)
        print(shortcut['shortLabel'] + ': ' + shortcut['packageName'])
        # intents
        shortcut['intents'] = {}
        # intents.act
        shortcut['intents']['act'] = reIntentsAct.search(s).group(1)
        # intents.dat(optional)
        intentsDat = reIntentsDat.search(s)
        if intentsDat:
            shortcut['intents']['dat'] = reIntentsDat.search(s).group(1)
        # intentsCmp(optional)
        intentsCmp = reIntentsCmp.search(s)
        if intentsCmp:
            shortcut['intents']['cmp'] = {}
            shortcut['intents']['cmp']['pkg'] = intentsCmp.group(1)
            shortcut['intents']['cmp']['cls'] = intentsCmp.group(2)
        # intentsCat(optional)
        intentsCat = reIntentsCat.search(s)
        if intentsCat:
            shortcut['intents']['cat'] = intentsCat.group(1)
        # intentsFlg(optional)
        intentsFlg = reIntentsFlg.search(s)
        if intentsFlg:
            shortcut['intents']['flg'] = intentsFlg.group(1)
        # intents.PersistableBundle(optinal)
        intentsPb = reIntentsPb.search(s)
        if intentsPb:
            pb = intentsPb.group(1)
            shortcut['intents']['pb'] = {}
            for pbIter in reIntentsPbAttr.finditer(pb):
                shortcut['intents']['pb'][pbIter.group(1)] = pbIter.group(2)
        # end
        shortcuts.append(shortcut)

with open('dumpsys-shortcut.json', mode='w') as f:
    f.write(json.dumps(shortcuts, indent=2, ensure_ascii=False))


