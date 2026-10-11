from pathlib import Path
import shutil, sys, xml.etree.ElementTree as ET
root=Path(sys.argv[1]); overlay=Path(__file__).parent/'overlay'
for src in overlay.rglob('*'):
    if src.is_file():
        dst=root/src.relative_to(overlay); dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
p=root/'main/src/ui/AndroidManifest.xml'
s=p.read_text(); s=s.replace('<category android:name="android.intent.category.LAUNCHER" />','').replace('<category android:name="android.intent.category.LEANBACK_LAUNCHER" />','')
s=s.replace('<application android:banner="@mipmap/banner_tv">','''<application android:banner="@mipmap/banner_tv">
<activity android:name=".activities.HiraActivity" android:exported="true" android:theme="@android:style/Theme.Material.NoActionBar">
<intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter>
</activity>'''); ET.fromstring(s); p.write_text(s)
p=root/'main/src/main/AndroidManifest.xml'; p.write_text(p.read_text().replace('android:allowBackup="true"','android:allowBackup="false"').replace('android:label="@string/app"','android:label="Hira VPN"'))
p=root/'main/build.gradle.kts'; s=p.read_text().replace('minSdk = 21','applicationId = "tech.hiragroup.vpn"\n        minSdk = 21'); p.write_text(s)
