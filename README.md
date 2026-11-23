# Trava Som

App Android que mantém fixos o modo do telefone (normal / vibrar / silencioso)
e os volumes de toque, ligação e alarme. Qualquer mudança feita fora do app
(botões de volume, painel de configurações rápidas, botão lateral) é desfeita
em cerca de 0,2 segundo.

## Gerar o APK

1. Instale o Android Studio e abra esta pasta (`TravaSom`).
2. Espere o Gradle sincronizar.
3. Menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. O arquivo fica em `app/build/outputs/apk/debug/app-debug.apk`.
5. Passe o APK para o celular da vovó e instale (permita "fontes desconhecidas").

Pela linha de comando: `gradle assembleDebug` (precisa do Android SDK instalado).

## Configurar no celular da vovó (uma vez só)

1. Abra o app e toque em **Permitir acesso a Não Perturbe**, depois ative o Trava Som.
2. Escolha o modo (Normal), ajuste os volumes de toque, ligação e alarme.
3. Coloque um PIN, para ela não conseguir abrir o app e mexer.
4. Ligue **Proteção ativa** e toque em **Salvar e aplicar**.
5. Em Configurações > Apps > Trava Som > Bateria, escolha **Sem restrições**
   (senão alguns celulares, como Xiaomi e Samsung, matam o serviço).
6. Aceite a notificação fixa "Trava Som ativo". Ela precisa existir para o Android
   manter o serviço rodando.

## Limites

- O Android não deixa um app comum bloquear os botões físicos. O app desfaz a
  mudança logo depois que ela acontece, então pode haver um "piscar" rápido.
- Se o modo Não Perturbe estiver ligado e a permissão não tiver sido dada,
  o app não consegue desfazer o silêncio.
- Projeto não testado em aparelho: foi escrito sem Android SDK disponível.
