# Merek

Aplikasi Pendaftaran Hak Merek milik Direktorat Jenderal Hak Kekayaan Intelektual (DIRJEN HAKI), Proyek di bawah Kementrian Hukum dan Hak Asasi Manusia Republik Indonesia

Hak Cipta milik PT. Docotel Technology Indonesia
<hr>
Lib yang kurang dapat diambil dari folder : _doc/repo<br>
Isi repo itu dicopy ke folder .m2 (untuk windows ada di folder : C:\Users\namauser\.m2\repository)<br>
untuk server centos ada di folder : /root/.m2/repository<br>
<br>
Remote connection ke database menggunakan vpn FortiClient.<br>
Buat koneksi di FortiClient dengan setting :<br>
- New Connection<br>
- Pilih: IPSec VPN<br>
<br/>
Koneksi :<br/>
- Remote Gateway: vpn1.dgip.go.id<br>
- username: docotel<br>
- pass: Biasa053710<br>
- preshared: fortinet123<br>
<br/>
Koneksi Baru : <br/>
* Remote gateway    : vpn1.dgip.go.id<br/>
* Username          : cendana<br/>
* Password          : cendanaP@ss<br/>
* Pre-shared key    : fortinet123<br/>

# Cara run di localhost windows<br>
```
cd C:/java/merek
mvnw spring-boot:run -P pq-exa-beta,no-rebuild-db,mail-dgip,simki
```

http://localhost:8080/djki/

# Cara build di localhost windows<br>
**BETA**:<br>
-PEMOHON<br>
`mvnw clean install -DskipTests -P pq-exa-beta,no-rebuild-db,mail-dgip,simki,general`<br>
-PETUGAS<br>
`mvnw clean install -DskipTests -P pq-exa-beta,no-rebuild-db,mail-dgip,simki,admin`<br>
<br>
**LIVE**:<br>
-PEMOHON<br>
`mvnw clean install -DskipTests -P pq-exa-tmnkita,no-rebuild-db,mail-dgip,simpaki,general,production`<br>
-PETUGAS<br>
`mvnw clean install -DskipTests -P pq-exa-tmnkita,no-rebuild-db,mail-dgip,simpaki,admin,production`<br>
<br>
output PEMOHON :<br>
/target/ROOT.war<br>
/target/ROOT.war.original<br>
<br>
output PETUGAS :<br>
/target/AdminMerek.war<br>
/target/AdminMerek.war.original<br>

# Cara upload .war PEMOHON ke beta / live<br>
connect ke server via FTP<br>
masuk ke path /opt/tomcat/webapps/<br>
upload file ROOT.war.original<br>
backup file ROOT.war yang ada di server, dengan cara rename menjadi ROOT.war-YYYYMMDD, contoh : ROOT.war-20210218<br>
rename ROOT.war.original menjadi ROOT.war<br>
change group owner file ROOT.war menjadi tomcat<br>

# Cara upload .war PETUGAS ke beta / live<br>
connect ke server via FTP<br>
masuk ke path /opt/tomcat/webapps/<br>
upload file AdminMerek.war.original<br>
backup file AdminMerek.war yang ada di server, dengan cara rename menjadi AdminMerek.war-YYYYMMDD, AdminMerek.war-20210218<br>
rename AdminMerek.war.original menjadi AdminMerek.war<br>
change group owner file AdminMerek.war menjadi tomcat<br>

# Cara build di server<br>
ssh ke server. jalankan perintah :<br>
```
sh build-pemohon.sh
sh build-petugas.sh
```
