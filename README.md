# TwitterDemo

> a uniApp for play

### Prerequisites

Before running the project, ensure you have the following installed:

- Android API  30+

---

### Step-by-Step Guide

1. Make sure connecting to your FireBase and installing the required dependencies

2. Create the CosConfig  ,  [here is guides to get Tencent cos config](https://cloud.tencent.com/document/product/436/65935)
   
   if you have Firebase storage better to ignore the example , [go to](https://firebase.google.com/docs/firestore/android/start/)
   
   ```java
   // example
   public interface CosConfig {
   
       // todo: hide the config
       String BUCKET = "xxxxxxx";
   
       String SECRETKEY = "xxxxx";
   
       String SECRETID = "xxxx";
   
       String REGION = "xxxxxx";
   }
   ```

3. Create the GoogleConfig for Realtime Database
   
   ```java
   // example
   public interface GoogleConfig {
   
       public String REALTIME_DATABASE_URL = "xxxxxxxxx";
   
   }
   ```

4. Some other configs  ( if you apply for google ads ) :
   
   ```xml
    ads_ticket.xml:
        ads:adUnitId="xxxxxxxxxx" 
    AndroidManifest.xml:
        android:value="xxxxxxxxx"
   ```

---

## Example for showing

<img src="./assets/example.gif" title="" alt="Logo" width="183">

---

## Contributing

1. Fork this repository.
2. Create a new branch: `git checkout -b feature/your-feature`.
3. Commit your changes: `git commit -am 'Add new feature'`.
4. Push to the branch: `git push origin feature/your-feature`.
5. Open a Pull Request.

We welcome all contributions, whether it's bug fixes, new features, or documentation improvements!
