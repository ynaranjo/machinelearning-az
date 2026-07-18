# 🌐 KidsGuard — Sitio web promocional

Landing page del producto en un **único archivo autocontenido**
(`index.html`): sin dependencias externas, responsive y con modo oscuro
automático. Español.

## Ver en local

Abre `index.html` en el navegador, o:

```bash
cd kidsguard-website
python3 -m http.server 8080   # http://localhost:8080
```

## Publicarlo

Cualquier hosting estático sirve. Opciones rápidas:

- **GitHub Pages**: copia `index.html` a la carpeta `docs/` de la rama
  principal y activa **Settings → Pages → Deploy from a branch → /docs**.
- **Netlify / Vercel / Cloudflare Pages**: arrastra la carpeta o conecta el
  repo apuntando a `kidsguard-website/`.

## Mantenimiento

- El botón **Descargar APK** apunta al workflow de GitHub Actions; cuando la
  app se publique en Play Store, cámbialo por el enlace de la ficha.
- Los enlaces del pie (código, manual, privacidad) apuntan a la rama de
  desarrollo; actualízalos si se fusiona a `master`.
- La paleta replica la de la app (`#4F6DF5`) con variante oscura.
