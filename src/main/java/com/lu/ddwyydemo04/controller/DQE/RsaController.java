package com.lu.ddwyydemo04.controller.DQE;


import cn.hutool.crypto.asymmetric.AsymmetricAlgorithm;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RsaController {

    private static final String PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMaelM9z71kRvHNUhPkFRadyiOUnVvFRFGX4mCPVo3AUT6nrIt+ndZkK4Z37OkZ+vKpEp4SRQ5WjQgRRdTiHcEgzqsAvzVG1U4c0b10urxkm3XwWR6vzSXvX1f6R+bL5gtkK12HNxZsN7uooWCV5PzJQkG7CQ5LpqKtsMKtyY+kLAgMBAAECgYABVoknGp9WqMVtu3iH2u68sYtVi93Mk2DBTRrtRt+qfiCJQwHBovWqWcXqVApw9iKcPQkp3faoF+j2cXwKLPNnLhKwY4zligmaSvDuLxLNm4N8vAqCqf71qNAwfAFVC7ptxEMvpNFnGgvSsAnX0S7Umcyy9mH06/Wf1OB3zLbieQJBAO/ELjDOZu7b0sHXW0QPKhIitkNxkJYtSADUPP9oYZWrhqc9SsR46l3oGfxYgLGGS7Hm3dNJ0RBDH0+BZt6b0FMCQQDUETXz0DAawJLKmaytdYAlx6zfvaJ2jS52jQkR8v3bl9UITeUq50O0/7gwVGJfjh41iYXefzv0VSScE3plk81pAkBC3q4bQpWGrJxdH5j+pFQRdsjqinPOzpY0VP0mJpCA07PcVMXTgf2rbx5AONa7rD7UQRVA5md+Z5oF7IsqLr6tAkEAnJQ8ltHfwtDvUYIX+lcokDyXSvgUrZ3ecY/+427buzinBaOmEoCBAHJTh/O2UsF3UqZA3qBpHyWCi27iT3/LCQJAJXtFMhROcLhkdbEkHyciZscQXiiVBHQbVtiYlW2vwqOXdIx7n2AyT8iVU67e49X3aAxOx4sBvZPNONiA3UriOw==";
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGnpTPc+9ZEbxzVIT5BUWncojlJ1bxURRl+Jgj1aNwFE+p6yLfp3WZCuGd+zpGfryqRKeEkUOVo0IEUXU4h3BIM6rAL81RtVOHNG9dLq8ZJt18Fker80l719X+kfmy+YLZCtdhzcWbDe7qKFgleT8yUJBuwkOS6airbDCrcmPpCwIDAQAB";
    private final RSA rsa = new RSA(AsymmetricAlgorithm.RSA_ECB_PKCS1.getValue(), PRIVATE_KEY, PUBLIC_KEY);

    @PostMapping("/encrypt")
    @ResponseBody
    public String encrypt(@RequestBody String text) {
        System.out.println("text:"+text);
        return rsa.encryptBase64(text, KeyType.PublicKey);
    }

    @PostMapping("/decrypt")
    @ResponseBody
    public String decrypt(@RequestBody String encryptedText) {
        try {
            encryptedText = encryptedText.trim();
            return rsa.decryptStr(encryptedText, KeyType.PrivateKey);
        } catch (Exception e) {
            return "解密失败：" + e.getMessage();
        }
    }

}
