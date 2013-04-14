İndir
=====

Sunucu uygulaması indirme bağlantısı : http://db.tt/ROmy5ep0

İstemci uygulaması indirme bağlantısı : http://db.tt/pTGyiapj

Paket Oluşturma
===============

Sunucu ve İstemci uygulamalarının paketleri aşağıdaki komutlar çalıştırıldığında
target dizininde oluşturulur. Komutlar için sistemde maven kurulu olmalıdır.

```bash
$ mvn package -P Server
$ mvn package -P Client
```

Uygulamanın Çalıştırılması
==========================

Paketler oluştuğunda, target dizininde Server-jar-with-dependencies.jar ve
Client-jar-with-dependencies.jar dosyaları oluşur.

Sunucu ve istemci grafiksel arabirime sahiptir. Çift tıklanarak ya da aşağıdaki
komutlar vasıtasıyla konsol üzerinden çalıştırılabilir:

```bash
$ java -jar target/Server-jar-with-dependencies.jar
...
$ java -jar target/Client-jar-with-dependencies.jar
```